package com.annasedykh.projectassistant.main;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.annasedykh.projectassistant.R;
import com.annasedykh.projectassistant.project.ProjectFile;
import com.annasedykh.projectassistant.project.ProjectsFragment;

public class MainPagerAdapter extends FragmentPagerAdapter {

    public static final int PAGE_CURRENT = 0;
    public static final int PAGE_FINISHED = 1;
    public static final int PAGE_ACCOUNTING = 2;

    private String[] titles;

    public MainPagerAdapter(FragmentManager fm, Context context) {
        super(fm);

        titles = context.getResources().getStringArray(R.array.tab_titles);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case PAGE_CURRENT:
                return ProjectsFragment.createProjectFragment(ProjectFile.TYPE_CURRENT);
            case PAGE_FINISHED:
                return ProjectsFragment.createProjectFragment(ProjectFile.TYPE_FINISHED);
            case PAGE_ACCOUNTING:
//                return BalanceFragment.createBalanceFragment();
                return ProjectsFragment.createProjectFragment(ProjectFile.TYPE_FINISHED);
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
       return titles[position];
    }
}
