package com.annasedykh.projectassistant;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_SIGN_IN = 0;


    private GoogleSignInClient googleSignInClient;
    private DriveClient driveClient;
    private DriveResourceClient driveResourceClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        signIn();

        Button logoutBtn = findViewById(R.id.logout);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(getString(R.string.logout), getString(R.string.dialog_msg), new MainActivity.LogoutDialogListener());
            }
        });
    }


    /**
     * Start sign in activity.
     */
    private void signIn() {
        Log.i(TAG, "Start sign in");
        googleSignInClient = buildGoogleSignInClient();
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
                    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
                    if (account != null) {
                        driveClient = Drive.getDriveClient(this, account);
                        driveResourceClient =
                                Drive.getDriveResourceClient(this, account);
                    }
                } else {
                    Log.i(TAG, "Sign in failed!");
                    showDialog(getString(R.string.exit), getString(R.string.dialog_msg), new MainActivity.ExitDialogListener());
                }
                break;
        }
    }

    private void logout() {
        googleSignInClient.signOut().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "Logout success!");
            }
        });
    }

    /** Show dialog */
    private void showDialog(String title, String message, DialogInterface.OnClickListener listener) {
        CommonDialog dialog = new CommonDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        dialog.setArguments(args);
        dialog.setListener(listener);
        dialog.show(getSupportFragmentManager(), "CommonDialog");
    }

    private class LogoutDialogListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    logout();
                    finish();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    dialog.cancel();
            }
        }
    }

    private class ExitDialogListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    finish();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    startActivityForResult(googleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
            }
        }
    }
}