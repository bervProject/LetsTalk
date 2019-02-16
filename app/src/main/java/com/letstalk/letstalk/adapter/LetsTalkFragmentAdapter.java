package com.letstalk.letstalk.adapter;

import com.letstalk.letstalk.fragment.ListenFragment;
import com.letstalk.letstalk.fragment.TalkFragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

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
