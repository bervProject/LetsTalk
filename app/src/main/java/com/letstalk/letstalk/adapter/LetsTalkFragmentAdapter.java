package com.letstalk.letstalk.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.letstalk.letstalk.fragment.ListenFragment;
import com.letstalk.letstalk.fragment.TalkFragment;

public class LetsTalkFragmentAdapter extends FragmentPagerAdapter {

    public LetsTalkFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        if (position == 0 ) {
            return ListenFragment.newInstance();
        } else  {
            return TalkFragment.newInstance();
        }
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return 2;
    }
}
