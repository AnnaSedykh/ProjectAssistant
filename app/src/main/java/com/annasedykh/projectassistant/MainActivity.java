package com.annasedykh.projectassistant;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static final String LOGOUT = "logout";
    public static final String SIGN_IN = "sign in";
    private static final int SIGN_IN_CODE = 1;
    private static final int LOGOUT_CODE = 2;

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
    }

    @Override
    protected void onResume() {
        super.onResume();

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
                    if(BuildConfig.DEBUG) {
                        Log.i(TAG, "Signed in successfully");
                    }
                } else {
                    if(BuildConfig.DEBUG) {
                        Log.i(TAG, "Sign in failed");
                    }
                    finish();
                }
                break;
            case LOGOUT_CODE:
                if (resultCode == RESULT_OK) {
                    if(BuildConfig.DEBUG) {
                        Log.i(TAG, "Logout successful");
                    }
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
}