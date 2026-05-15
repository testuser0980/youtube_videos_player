package com.imran.ytplayer;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MainPagerAdapter extends FragmentStateAdapter {

    public MainPagerAdapter(MainActivity activity) {
        super(activity);
    }

    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new PlaylistFragment();
            case 1: return VideoListFragment.newInstance(VideoListFragment.TYPE_SUBSCRIPTIONS);
            default: return new PlaylistFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
