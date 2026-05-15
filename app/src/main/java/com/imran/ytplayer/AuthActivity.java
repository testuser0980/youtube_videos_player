package com.imran.ytplayer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;

public class AuthActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1001;
    private static final String TAG = "AuthActivity";
    private GoogleSignInClient googleSignInClient;
    private PrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        prefsManager = new PrefsManager(this);

        try {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestProfile()
                    .build();
            googleSignInClient = GoogleSignIn.getClient(this, gso);
        } catch (Exception e) {
            Log.e(TAG, "Failed to create GoogleSignInClient: " + e.getMessage());
            Toast.makeText(this, "Google Sign-In not configured. Please use API key only.", Toast.LENGTH_LONG).show();
        }

        MaterialButton btnGoogleSignIn = findViewById(R.id.btn_google_signin);
        MaterialButton btnSkip = findViewById(R.id.btn_skip);

        btnGoogleSignIn.setOnClickListener(v -> signIn());
        btnSkip.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        // Check if already signed in
        try {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            if (account != null) {
                handleSignInSuccess(account);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking last signed in account: " + e.getMessage());
        }
    }

    private void signIn() {
        if (googleSignInClient == null) {
            Toast.makeText(this, "Google Sign-In is not available. Make sure google-services.json is configured.", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        } catch (Exception e) {
            Log.e(TAG, "Sign in intent error: " + e.getMessage());
            Toast.makeText(this, "Sign in error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            try {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                GoogleSignInAccount account = task.getResult(ApiException.class);
                handleSignInSuccess(account);
            } catch (ApiException e) {
                Log.e(TAG, "Sign in failed. Status code: " + e.getStatusCode(), e);
                String errorMsg = "Sign in failed (code: " + e.getStatusCode() + ")";
                if (e.getStatusCode() == 10) {
                    errorMsg = "Sign in failed: Developer error. Check your google-services.json and SHA-1 fingerprint in Google Cloud Console.";
                } else if (e.getStatusCode() == 12500) {
                    errorMsg = "Sign in failed: App not configured properly. Check Google Cloud Console setup.";
                } else if (e.getStatusCode() == 12501) {
                    errorMsg = "Sign in cancelled by user.";
                } else if (e.getStatusCode() == 7) {
                    errorMsg = "Sign in failed: Network error. Check your internet connection.";
                }
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Log.e(TAG, "Sign in error: " + e.getMessage(), e);
                Toast.makeText(this, "Sign in error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void handleSignInSuccess(GoogleSignInAccount account) {
        prefsManager.setSignedIn(true);
        prefsManager.setUserName(account.getDisplayName());
        prefsManager.setUserEmail(account.getEmail());
        Toast.makeText(this, "Welcome " + account.getDisplayName(), Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }
}
