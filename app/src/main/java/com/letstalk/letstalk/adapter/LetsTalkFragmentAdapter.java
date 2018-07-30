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
        if (position == 0 ) {
            return ListenFragment.newInstance();
        } else  {
            return TalkFragment.newInstance();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
