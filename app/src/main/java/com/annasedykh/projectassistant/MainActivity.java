package com.annasedykh.projectassistant;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.annasedykh.projectassistant.service.ProjectService;
import com.annasedykh.projectassistant.service.ProjectServiceImpl;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.Scopes;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;

import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static final String LOGOUT = "logout";
    public static final String SIGN_IN = "sign in";
    private static final int SIGN_IN_CODE = 1;
    private static final int LOGOUT_CODE = 2;

    private GoogleAccountCredential credential;
    private Drive driveService;
    private ProjectService projectService;

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private FloatingActionButton fab;
    private MainPagerAdapter pagerAdapter = null;
    private ActionMode actionMode = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbarAndTabs();

        Intent signInIntent = new Intent(this, AuthActivity.class);
        signInIntent.putExtra(SIGN_IN, true);
        startActivityForResult(signInIntent, SIGN_IN_CODE);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void initToolbarAndTabs() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.main_screen_title);
        setSupportActionBar(toolbar);

        viewPager = findViewById(R.id.view_pager);
        pagerAdapter = new MainPagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(pagerAdapter);

        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem item = menu.findItem(R.id.logout);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                CommonDialog.show(getString(R.string.logout), getString(R.string.dialog_msg), new MainActivity.LogoutDialogListener(), getSupportFragmentManager());
                return true;
            }
        });
        return true;
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SIGN_IN_CODE:
                Log.i(TAG, "Sign in finished");
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "Signed in successfully");
                    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
                    if (account != null) {
                        credential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(Scopes.DRIVE_FULL));
                        credential.setSelectedAccountName("anna.emulator@gmail.com");

                        driveService = new Drive.Builder(
                                AndroidHttp.newCompatibleTransport(),
                                JacksonFactory.getDefaultInstance(),
                                credential
                        ).setApplicationName(getString(R.string.app_name))
                                .build();
                        projectService = new ProjectServiceImpl(driveService);
                    }
                } else {
                    Log.i(TAG, "Sign in failed");
                    finish();
                }
                break;
            case LOGOUT_CODE:
                Log.i(TAG, "Logout finished");
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "Logout successful");
                    finish();
                }
                break;
        }
    }

    public class LogoutDialogListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    Intent logoutIntent = new Intent(MainActivity.this, AuthActivity.class);
                    logoutIntent.putExtra(LOGOUT, true);
                    startActivityForResult(logoutIntent, LOGOUT_CODE);
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    dialog.cancel();
            }
        }
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public GoogleAccountCredential getCredential() {
        return credential;
    }

    public Drive getDriveService() {
        return driveService;
    }

    public ProjectService getProjectService() {
        return projectService;
    }
}