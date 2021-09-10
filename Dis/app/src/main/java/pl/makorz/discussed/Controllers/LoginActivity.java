package pl.makorz.discussed.Controllers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import pl.makorz.discussed.R;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int RC_GOOGLE_SIGN_IN = 496;

    private CheckBox checkTermsBox, checkAgeBox;
    private int USER_NEW_ACCOUNT = 10;

    private SignInButton buttonSignInGoogle;
    private LoginButton buttonSignInFacebook;

    private GoogleSignInClient client;
    private FirebaseAuth mAuth;
    FirebaseUser user;

    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Client object
        client = GoogleSignIn.getClient(this, gso);
        // Button google sign in
        googleButtonWork();
        // Button facebook sign in
        facebookButtonWork();
        // Check Terms And Conditions
        termsAgreementCheck();
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                if (user == null) {
                    USER_NEW_ACCOUNT = -1;
                    Log.w(TAG, "User is null, not going to navigate");
                }
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
        // Result returned from launching the Intent
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void firebaseAuthWithGoogle(String idToken) {

        buttonSignInFacebook.setEnabled(false);
        buttonSignInGoogle.setEnabled(false);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            boolean isNew = Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getAdditionalUserInfo()).isNewUser();
                            if (isNew) {
                                USER_NEW_ACCOUNT = 0;
                            }
                            user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, getString(R.string.authentication_failed_login_activity), Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {

        Log.d(TAG, "handleFacebookAccessToken:" + token);
        buttonSignInFacebook.setEnabled(false);
        buttonSignInGoogle.setEnabled(false);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {


                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            boolean isNew = Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getAdditionalUserInfo()).isNewUser();
                            if (isNew) {
                                USER_NEW_ACCOUNT = 0;
                            }
                            user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);


    }

    private void updateUI(FirebaseUser currentUser) {
        //Navigate to Main Activity
        if (currentUser == null) {
            Log.w(TAG, "User is null, not going to navigate2");
            return;
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("USER_NEW_ACCOUNT",USER_NEW_ACCOUNT);
        intent.putExtra("USER_LOGIN_DATE", new Date(new Date().getTime()));

        Log.d(TAG, String.valueOf(USER_NEW_ACCOUNT));

        startActivity(intent);
        // Don't want to show login in back stag
        finish();

    }

    private void facebookButtonWork() {

        buttonSignInFacebook = (LoginButton) findViewById(R.id.button_sign_in_facebook);
        buttonSignInFacebook.setEnabled(false);
        buttonSignInFacebook.setReadPermissions(Arrays.asList("email"));
        buttonSignInFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "facebook:onSuccess:");
                LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(TAG, "facebook:onSuccess:" + loginResult);
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "facebook:onCancel");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.d(TAG, "facebook:onError", exception);

                    }
                });
            }
        });
    }

    private void googleButtonWork() {
        buttonSignInGoogle = findViewById(R.id.button_sign_in_google);
        buttonSignInGoogle.setEnabled(false);
        buttonSignInGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = client.getSignInIntent();
                startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
            }
        });
    }

    private void termsAgreementCheck () {
        TextView termsTextView = findViewById(R.id.textOfTerms);
        checkTermsBox = findViewById(R.id.checkAgreeTerms);
        checkAgeBox = findViewById(R.id.checkAgreeAge);
        checkTermsBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                buttonSignInGoogle.setEnabled(checkTermsBox.isChecked() && checkAgeBox.isChecked());
                buttonSignInFacebook.setEnabled(checkTermsBox.isChecked() && checkAgeBox.isChecked());
            }
        });
        checkAgeBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                buttonSignInGoogle.setEnabled(checkTermsBox.isChecked() && checkAgeBox.isChecked());
                buttonSignInFacebook.setEnabled(checkTermsBox.isChecked() && checkAgeBox.isChecked());
            }
        });
        termsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                termsAndConditionsAlertDialog();
            }
        });
    }

    public void termsAndConditionsAlertDialog() {

        LayoutInflater inflaterDialog = LayoutInflater.from(this);
        View termsDialogView = inflaterDialog.inflate(R.layout.terms_dialog_box, null);

        AlertDialog termsDialog = new AlertDialog.Builder(this)
                .setView(termsDialogView)  // What to use in dialog box
                .setPositiveButton(R.string.understand_accept_terms_login_activity, null)
                .show();

        termsDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    checkTermsBox.setChecked(true);
                    termsDialog.dismiss();
            }
        });
    }

}