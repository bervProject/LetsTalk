package com.letstalk.letstalk.adapter

import com.letstalk.letstalk.fragment.ListenFragment
import com.letstalk.letstalk.fragment.TalkFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class LetsTalkFragmentAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return if (position == 0) {
            ListenFragment.newInstance()
        } else {
            TalkFragment.newInstance()
        }
    }

    override fun getCount(): Int {
        return 2
    }
}
