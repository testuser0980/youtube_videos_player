package com.imran.ytplayer;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;

public class SettingsActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1001;

    private ImageView profileAvatar;
    private TextView profileName, profileEmail, qualityValue;
    private MaterialButton btnSignIn, btnSignOut;
    private ImageButton btnBack;
    private LinearLayout settingApiKey, settingQuality, settingHistory, settingAbout;

    private PrefsManager prefsManager;
    private GoogleSignInClient googleSignInClient;
    private GoogleSignInAccount signedInAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefsManager = new PrefsManager(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestServerAuthCode("831334231930-u15c9s5lofur26lap5iav5al1t8ik1gd.apps.googleusercontent.com")
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        initViews();
        updateUI();
    }

    private void initViews() {
        profileAvatar = findViewById(R.id.profile_avatar);
        profileName = findViewById(R.id.profile_name);
        profileEmail = findViewById(R.id.profile_email);
        btnSignIn = findViewById(R.id.btn_sign_in);
        btnSignOut = findViewById(R.id.btn_sign_out);
        btnBack = findViewById(R.id.btn_back);
        settingApiKey = findViewById(R.id.setting_api_key);
        settingQuality = findViewById(R.id.setting_quality);
        settingHistory = findViewById(R.id.setting_history);
        settingAbout = findViewById(R.id.setting_about);
        qualityValue = findViewById(R.id.quality_value);

        btnBack.setOnClickListener(v -> finish());
        btnSignIn.setOnClickListener(v -> signIn());
        btnSignOut.setOnClickListener(v -> signOut());

        settingApiKey.setOnClickListener(v -> showApiKeyDialog());
        settingQuality.setOnClickListener(v -> showQualityDialog());
        settingHistory.setOnClickListener(v -> showHistoryDialog());
        settingAbout.setOnClickListener(v -> showAboutDialog());
    }

    private void updateUI() {
        if (prefsManager.isSignedIn()) {
            profileName.setText(prefsManager.getUserName());
            profileEmail.setText(prefsManager.getUserEmail());
            btnSignIn.setVisibility(View.GONE);
            btnSignOut.setVisibility(View.VISIBLE);
        } else {
            profileName.setText("Sign in to YouTube");
            profileEmail.setText("");
            btnSignIn.setVisibility(View.VISIBLE);
            btnSignOut.setVisibility(View.GONE);
        }
    }

    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            prefsManager.setSignedIn(false);
            prefsManager.setUserName("");
            prefsManager.setUserEmail("");
            signedInAccount = null;
            updateUI();
            Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                signedInAccount = account;
                prefsManager.setSignedIn(true);
                prefsManager.setUserName(account.getDisplayName());
                prefsManager.setUserEmail(account.getEmail());
                updateUI();
                Toast.makeText(this, "Signed in as " + account.getDisplayName(), Toast.LENGTH_SHORT).show();

                // Return the account info to MainActivity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("account_email", account.getEmail());
                resultIntent.putExtra("account_name", account.getDisplayName());
                setResult(RESULT_OK, resultIntent);
            }
        } catch (ApiException e) {
            Toast.makeText(this, "Sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
        }
    }

    private void showApiKeyDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_api_key, null);
        android.widget.EditText apiKeyInput = dialogView.findViewById(R.id.api_key_input);
        apiKeyInput.setText(prefsManager.getApiKey());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_save).setOnClickListener(v -> {
            String key = apiKeyInput.getText().toString().trim();
            prefsManager.setApiKey(key);
            Toast.makeText(this, "API Key saved", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showQualityDialog() {
        String[] qualities = {"Auto", "1080p", "720p", "480p", "360p", "240p", "144p"};
        new AlertDialog.Builder(this)
                .setTitle("Video Quality")
                .setItems(qualities, (dialog, which) -> {
                    qualityValue.setText(qualities[which]);
                    Toast.makeText(this, "Quality set to " + qualities[which], Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showHistoryDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Watch History")
                .setMessage("Clear all watch history?")
                .setPositiveButton("Clear", (dialog, which) -> {
                    prefsManager.clearAll();
                    Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("About YT Player")
                .setMessage("YT Player v1.0\n\nA YouTube video player app built with Java.")
                .setPositiveButton("OK", null)
                .show();
    }
}
