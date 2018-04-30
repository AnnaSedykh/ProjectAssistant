package com.annasedykh.projectassistant;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.tasks.OnSuccessListener;

public class AuthActivity extends AppCompatActivity {

    private static final String TAG = "AuthActivity";
    private static final int REQUEST_CODE_SIGN_IN = 0;

    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(googleSignInClient == null){
            googleSignInClient = buildGoogleSignInClient();
        }

        boolean isLogoutIntent = getIntent().getBooleanExtra(MainActivity.LOGOUT, false);
        boolean isSigninIntent = getIntent().getBooleanExtra(MainActivity.SIGN_IN, false);

        if(isLogoutIntent){
            logout();
        } else if(isSigninIntent){
            signIn();
        }
    }

    /**
     * Start sign in activity.
     */
    private void signIn() {
        Log.i(TAG, "Start sign in");
        startActivityForResult(googleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

    /**
     * Build a Google SignIn client.
     */
    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_FILE)
                        .build();
        return GoogleSignIn.getClient(this, signInOptions);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                Log.i(TAG, "Sign in request code");
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "Signed in successfully.");
                    setResult(RESULT_OK, getIntent());
                    finish();
                } else {
                    Log.i(TAG, "Sign in failed!");
                    CommonDialog.show(getString(R.string.exit), getString(R.string.dialog_msg), new AuthActivity.ExitDialogListener(), getSupportFragmentManager());
                }
                break;
        }
    }

    private void logout() {
        Log.i(TAG, "Start logout");
        googleSignInClient.signOut().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "Logout success!");
                setResult(RESULT_OK, getIntent());
                finish();
            }
        });
    }

    private class ExitDialogListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    setResult(RESULT_CANCELED, getIntent());
                    finish();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    startActivityForResult(googleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
            }
        }
    }
}