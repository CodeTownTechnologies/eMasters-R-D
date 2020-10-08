package com.app.sociallogin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.OAuthProvider;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.http.IHttpRequest;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.requests.extensions.GraphServiceClient;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String CONSUMER_KEY_YAHOO =
            "dj0yJmk9MlFLc1lKRGRya2huJmQ9WVdrOVdqYzFNMFpTZVdnbWNHbzlNQT09JnM9Y29uc3VtZXJzZWNyZXQmc3Y9MCZ4PTBi";

    private static final String CONSUMER_SECRET_YAHOO =
            "ad5d41fe988c894dfdfdbdf9b7e43105764fd8d6";

    private static final String REDIRECT_URI = "com.app.yahoologin://login";

    private static final int REQUEST = 4;

    private static final int GOOGLE_SIGN_IN_REQUEST = 12;
    private GoogleSignInClient googleSignInClient;
    private String name, email, fbId, googleId, imageUrl, phone;


    Button btnGoogleLogin;
    Button btnOutlookLogin;
    Button btnYahooLogin;
    Button btnLogout;

    TextView tvMessage;

    private ProgressDialog progress;
    private String yahooAccessToken = "";
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        btnOutlookLogin = findViewById(R.id.btnOutlookLogin);
        btnYahooLogin = findViewById(R.id.btnYahooLogin);
        btnLogout = findViewById(R.id.btnLogout);
        tvMessage = findViewById(R.id.tvMessage);

        Intent intent = getIntent();


        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            //yahoo code
            /*if (uri != null) {
                String authCode = uri.getQueryParameter("code");
                Toast.makeText(this, authCode, Toast.LENGTH_LONG).show();
                requestyahooAccessToken(authCode);
            }*/
        }

        mAuth = FirebaseAuth.getInstance();

        onLogout();


        btnGoogleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onGoogleLogin();
            }
        });

        btnYahooLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onYahooLogin();

            }
        });

        btnOutlookLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onOutlookLogin();
            }
        });


        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onLogout();
            }
        });

    }

    private void onLogout() {

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            FirebaseAuth.getInstance().signOut();
        }

    }

    /*Google Login*/

    private void onGoogleLogin() {


        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .requestId()
                .build();


        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        startProgressBar();


        switch (requestCode) {

            case GOOGLE_SIGN_IN_REQUEST:

                if (resultCode == RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);


                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);

                        googleId = account.getId();
                        email = account.getEmail();
                        name = account.getDisplayName();

                        imageUrl = "";

                        if (account.getPhotoUrl() != null) {
                            imageUrl = account.getPhotoUrl().toString();
                        }

                        tvMessage.setText("Google Details " +
                                "\n\n" +
                                account.getDisplayName() +
                                "\n" +
                                account.getEmail() /*+
                                "\n" +
                                account.getIdToken()*/);


                        dismissProgressBar();


                    } catch (ApiException e) {
                        dismissProgressBar();
                        e.printStackTrace();
                    }
                }

                break;

        }


        dismissProgressBar();
    }


    /*Outlook Login*/
    private void onOutlookLogin() {

        OAuthProvider.Builder provider = OAuthProvider.newBuilder("microsoft.com");

        List<String> scopes =
                new ArrayList<String>() {
                    {
                        add("Contacts.Read");
                    }
                };
        provider.setScopes(scopes);

        Task<AuthResult> pendingResultTask = mAuth.getPendingAuthResult();
        if (pendingResultTask != null) {
            // There's something already here! Finish the sign-in for your user.
            pendingResultTask
                    .addOnSuccessListener(
                            new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    // User is signed in.
                                    // IdP data available in
                                    // authResult.getAdditionalUserInfo().getProfile().
                                    // The OAuth access token can also be retrieved:
                                    // authResult.getCredential().getAccessToken().
                                    // The OAuth ID token can also be retrieved:
                                    // authResult.getCredential().getIdToken().
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Handle failure.
                                }
                            });
        } else {
            // There's no pending result so you need to start the sign-in flow.
            // See below.

            mAuth.startActivityForSignInWithProvider(/* activity= */ this, provider.build())
                    .addOnSuccessListener(
                            new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    // User is signed in.
                                    // IdP data available in
                                    // authResult.getAdditionalUserInfo().getProfile().
                                    // The OAuth access token can also be retrieved:
                                    // authResult.getCredential().getAccessToken().
                                    // The OAuth ID token can also be retrieved:
                                    // authResult.getCredential().getIdToken().
                                    if (authResult.getAdditionalUserInfo() != null &&
                                            authResult.getAdditionalUserInfo().getProfile() != null) {
                                        for (int i = 0; i < authResult.getAdditionalUserInfo().getProfile().size(); i++) {
                                            authResult.getUser();
                                        }
                                        if (mAuth.getCurrentUser() != null) {
                                            //String userId = firebaseAuth.getCurrentUser().getProviderData().get(0).getUid();

                                            authResult.getUser().getProviderData().get(1).getUid();


                                            //getYahooProfile("");

                                        }
                                    }
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Handle failure.
                                }
                            });
        }


    }

    /*Outlook Login without Firebase*/
    private void onOutlookLoginWithoutFirebase(){

        IAuthenticationProvider authProvider = new IAuthenticationProvider() {
            @Override
            public void authenticateRequest(IHttpRequest request) {
            }
        };

        IGraphServiceClient graphClient =
                GraphServiceClient
                        .builder()
                        .authenticationProvider(authProvider)
                        .buildClient();

    }

    /*Yahoo Login*/

    private void onYahooLogin() {
        OAuthProvider.Builder provider = OAuthProvider.newBuilder("yahoo.com");

        // Prompt user to re-authenticate to Yahoo.
        provider.addCustomParameter("prompt", "login");

        // Localize to French.
        provider.addCustomParameter("language", "en");

        List<String> scopes =
                new ArrayList<String>() {
                    {
                        // This must be preconfigured in the app's API permissions.
                        add("sdct-r");
                        add("profile");
                        add("email");
                    }
                };
        provider.setScopes(scopes);


        Task<AuthResult> pendingResultTask = mAuth.getPendingAuthResult();
        if (pendingResultTask != null) {
            // There's something already here! Finish the sign-in for your user.
            pendingResultTask
                    .addOnSuccessListener(
                            new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    // User is signed in.
                                    // IdP data available in
                                    // authResult.getAdditionalUserInfo().getProfile().
                                    // The OAuth access token can be retrieved:
                                    // authResult.getCredential().getAccessToken().
                                    // Yahoo OAuth ID token can be retrieved:
                                    // authResult.getCredential().getIdToken().
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Handle failure.
                                }
                            });
        } else {
            // There's no pending result so you need to start the sign-in flow.
            // See below.

            mAuth
                    .startActivityForSignInWithProvider(this, provider.build())
                    .addOnSuccessListener(
                            new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    // User is signed in.
                                    // IdP data available in
                                    // authResult.getAdditionalUserInfo().getProfile().
                                    // The OAuth access token can be retrieved:
                                    // authResult.getCredential().getAccessToken().
                                    // Yahoo OAuth ID token can also be retrieved:
                                    // authResult.getCredential().getIdToken().
                                    if (authResult.getAdditionalUserInfo() != null &&
                                            authResult.getAdditionalUserInfo().getProfile() != null) {

                                        tvMessage.setText("Yahoo Details " +
                                                "\n\n" +
                                                authResult.getAdditionalUserInfo().getProfile().get("name").toString() +
                                                "\n" +
                                                authResult.getAdditionalUserInfo().getProfile().get("email").toString());

                                        //getYahooProfile("");
                                    }
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Handle failure.
                                }
                            });
        }
    }

    private void getYahooProfile(String authToken) {

        progress = new ProgressDialog(this);
        progress.setTitle(R.string.app_name);
        progress.setMessage("Loading...");
        progress.setCancelable(false);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<ResponseBody> loginCall = apiInterface.getYahooProfile(
                "Bearer " + authToken);
        loginCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progress.dismiss();
                if (response.body() != null) {

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("onFailure: ", t.getLocalizedMessage());
                progress.dismiss();
            }
        });
    }



    /*Yahoo login without firebase*/

    /*private void yahooLoginWithoutFirebase() {

        String url = Constants.YAHOO_BASE_URL +
                Constants.YAHOO_OAUTH + "" +
                "request_auth?" +
                "client_id=" + CONSUMER_KEY_YAHOO +
                "&redirect_uri=" + REDIRECT_URI +
                "&response_type=code" +
                "&language=en-us";
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        if (browserIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(browserIntent);
        }

    }

    private void requestYahooAccessToken(String authCode) {

        progress = new ProgressDialog(this);
        progress.setTitle(R.string.app_name);
        progress.setMessage("Loading...");
        progress.setCancelable(false);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<YahooAccessTokenResponse> apiCall = apiInterface.getYahooToken(CONSUMER_KEY_YAHOO,
                CONSUMER_SECRET_YAHOO, REDIRECT_URI,
                authCode, "authorization_code");
        apiCall.enqueue(new Callback<YahooAccessTokenResponse>() {
            @Override
            public void onResponse(Call<YahooAccessTokenResponse> call, Response<YahooAccessTokenResponse> response) {
                progress.dismiss();
                if (response.isSuccessful()) {
                    YahooAccessTokenResponse yahooAccessTokenResponse = response.body();

                    if (yahooAccessTokenResponse != null && yahooAccessTokenResponse.getAccessToken() != null) {
                        getYahooContacts(yahooAccessTokenResponse.getAccessToken(),
                                yahooAccessTokenResponse.getXoauthYahooGuid());
                    }
                } else {

                }
            }

            @Override
            public void onFailure(Call<YahooAccessTokenResponse> call, Throwable t) {
                progress.dismiss();
            }
        });

    }

    private void getYahooContacts(String authToken, String userId) {

        progress = new ProgressDialog(this);
        progress.setTitle(R.string.app_name);
        progress.setMessage("Loading...");
        progress.setCancelable(false);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<ResponseBody> loginCall = apiInterface.getYahooContacts(
                "Bearer " + authToken, userId, "json");
        loginCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progress.dismiss();
                if (response.body() != null) {

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("onFailure: ", t.getLocalizedMessage());
                progress.dismiss();
            }
        });
    }*/

    private void startProgressBar() {
        progress = new ProgressDialog(this);
        progress.setTitle(R.string.app_name);
        progress.setMessage("Loading...");
        progress.setCancelable(false);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();
    }

    private void dismissProgressBar() {
        if (progress != null && progress.isShowing()) {
            progress.dismiss();
        }
    }
}