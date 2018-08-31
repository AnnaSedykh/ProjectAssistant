package com.annasedykh.projectassistant.main;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.annasedykh.projectassistant.BuildConfig;
import com.annasedykh.projectassistant.R;
import com.annasedykh.projectassistant.app.App;
import com.annasedykh.projectassistant.auth.AuthActivity;
import com.annasedykh.projectassistant.service.ProjectService;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * {@link MainActivity} shows 2 tabs with current and finished project folders
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static final String LOGOUT = "logout";
    public static final String SIGN_IN = "sign in";
    private static final int SIGN_IN_CODE = 1;
    private static final int LOGOUT_CODE = 2;
    private ProjectService projectService;
    /**
     * true if read permission request was sent
     */
    private boolean isAccessRequestSent = false;

    @BindView(R.id.view_pager)
    ViewPager viewPager;
    @BindView(R.id.tab_layout)
    TabLayout tabLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initToolbarAndTabs();

        Intent signInIntent = new Intent(this, AuthActivity.class);
        signInIntent.putExtra(SIGN_IN, true);
        startActivityForResult(signInIntent, SIGN_IN_CODE);
        projectService = ((App) getApplication()).getProjectService();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        new ReceiveChangesTask().execute();
    }

    private void initToolbarAndTabs() {
        toolbar.setTitle(R.string.main_screen_title);
        setSupportActionBar(toolbar);

        MainPagerAdapter pagerAdapter = new MainPagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(pagerAdapter);
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
                if (resultCode == RESULT_OK) {
                    if (BuildConfig.DEBUG) {
                        Log.i(TAG, "Signed in successfully");
                    }
                } else {
                    if (BuildConfig.DEBUG) {
                        Log.i(TAG, "Sign in failed");
                    }
                    finish();
                }
                break;
            case LOGOUT_CODE:
                if (resultCode == RESULT_OK) {
                    if (BuildConfig.DEBUG) {
                        Log.i(TAG, "Logout successful");
                    }
                    finish();
                }
                break;
        }
    }

    public boolean isAccessRequestSent() {
        return isAccessRequestSent;
    }

    public void setAccessRequestSent(boolean accessRequestSent) {
        isAccessRequestSent = accessRequestSent;
    }

    /**
     * Task to receive last changes asynchronously
     */
    @SuppressLint("StaticFieldLeak")
    private class ReceiveChangesTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            projectService.receiveLastChanges();
            return null;
        }
    }

    /**
     * {@link LogoutDialogListener} starts logout activity or dismiss.
     */
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
}