package com.github.goldfish07.hydra.vpn.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.github.goldfish07.hydra.vpn.fragment.FreeServersFragment;
import com.github.goldfish07.hydra.vpn.fragment.PaidServerFragment;


public class ViewPagerAdapter extends FragmentPagerAdapter {

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        if (position == 0)
        {
            fragment = new FreeServersFragment();
        }
        else if (position == 1)
        {
            fragment = new PaidServerFragment();
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = null;
        if (position == 0)
        {
            title = "Tab-1";
        }
        else if (position == 1)
        {
            title = "Tab-2";
        }
        else if (position == 2)
        {
            title = "Tab-3";
        }
        return title;
    }
}