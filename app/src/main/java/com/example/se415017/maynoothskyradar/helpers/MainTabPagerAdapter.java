package com.example.se415017.maynoothskyradar.helpers;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.se415017.maynoothskyradar.fragments.AircraftListFragment;
import com.example.se415017.maynoothskyradar.fragments.MainMapFragment;
import com.example.se415017.maynoothskyradar.objects.Aircraft;

/**
 * Created by se415017 on 03/03/2016.
 *
 * This is just a way for me to put all of the methods for creating a PagerAdapter for my
 * MainActivity into their own class.
 */
public class MainTabPagerAdapter extends FragmentStatePagerAdapter {
    private final String[] TAB_TITLES = {"Map", "List of planes"};
    FragmentManager fragMgr;

    public MainTabPagerAdapter(FragmentManager fm) {
        super(fm);
        fragMgr = fm;
    }

    @Override
    public CharSequence getPageTitle(int position){
        return TAB_TITLES[position];
    }

    @Override
    public int getCount() {
        return TAB_TITLES.length;
    }

    @Override
    public Fragment getItem(int position){
        switch(position){
            case 0:
                return MainMapFragment.newInstance();
            case 1:
                return AircraftListFragment.newInstance(1);
        }
        return null;
    }

}
