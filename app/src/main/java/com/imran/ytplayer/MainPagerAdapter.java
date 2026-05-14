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
            case 0: return VideoListFragment.newInstance(VideoListFragment.TYPE_TRENDING);
            case 1: return VideoListFragment.newInstance(VideoListFragment.TYPE_SUBSCRIPTIONS);
            case 2: return new PlaylistFragment();
            case 3: return VideoListFragment.newInstance(VideoListFragment.TYPE_LIBRARY);
            default: return VideoListFragment.newInstance(VideoListFragment.TYPE_TRENDING);
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
