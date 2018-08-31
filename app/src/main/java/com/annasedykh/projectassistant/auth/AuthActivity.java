package com.annasedykh.projectassistant.auth;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.annasedykh.projectassistant.BuildConfig;
import com.annasedykh.projectassistant.R;
import com.annasedykh.projectassistant.main.CommonDialog;
import com.annasedykh.projectassistant.main.MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * {@link AuthActivity} is used to sign in to Google account and logout.
 */
public class AuthActivity extends AppCompatActivity {

    private static final String TAG = "AuthActivity";
    private static final int REQUEST_CODE_SIGN_IN = 0;

    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (googleSignInClient == null) {
            googleSignInClient = buildGoogleSignInClient();
        }

        boolean isLogoutIntent = getIntent().getBooleanExtra(MainActivity.LOGOUT, false);
        boolean isSigninIntent = getIntent().getBooleanExtra(MainActivity.SIGN_IN, false);

        if (isLogoutIntent) {
            logout();
        } else if (isSigninIntent) {
            signIn();
        }
    }

    /**
     * Start sign in activity.
     */
    private void signIn() {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Start sign in");
        }
        startActivityForResult(googleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

    /**
     * Build a Google SignIn client.
     */
    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(new Scope(Scopes.DRIVE_FULL))
                        .build();
        return GoogleSignIn.getClient(this, signInOptions);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                if (resultCode == RESULT_OK) {
                    setResult(RESULT_OK, getIntent());
                    finish();
                } else {
                    CommonDialog.show(getString(R.string.exit), getString(R.string.dialog_msg), new AuthActivity.ExitDialogListener(), getSupportFragmentManager());
                }
                break;
        }
    }

    /**
     * Sign out from account and finish activity.
     */
    private void logout() {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Start logout");
        }
        googleSignInClient.signOut().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                setResult(RESULT_OK, getIntent());
                finish();
            }
        });
    }

    /**
     * {@link ExitDialogListener} finishes the app or starts sign in activity again.
     */
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