package com.chiri.finalmusicplayer.adapters;

/**
 * Created by chiri on 20/04/17.
 */

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.chiri.finalmusicplayer.fragments.PageFragment;
import com.chiri.finalmusicplayer.R;

public class PagerAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 3;
    private String typeTabs[];
    private Context context;

    public PagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
        typeTabs = new String[]{context.getString(R.string.title_tab1),
                                context.getString(R.string.title_tab2),
                                context.getString(R.string.title_tab3)};
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        Log.i("PagerAdapter ", "Position = "+ position +" Type = "+typeTabs[position]);
        return PageFragment.newInstance(position, typeTabs[position]);
    }

}