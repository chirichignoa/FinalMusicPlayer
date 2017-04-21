package com.chiri.finalmusicplayer;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

public class LibraryActivity extends AppCompatActivity {
    private static CursorAdapter cursor;
    private ViewPager viewPager;
    private TabLayout tabs;
    private FragmentPagerAdapter adapterViewPager;
    private Integer arrDrawables[] = new Integer[] { R.drawable.ic_music_note,
                                                 R.drawable.ic_album,
                                                 R.drawable.ic_playlist_play };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        setupTabLayout();
    }

    private void setupTabLayout() {
        viewPager = (ViewPager) findViewById(R.id.pager);
        this.adapterViewPager = new PagerAdapter(getSupportFragmentManager(), getApplicationContext());
        viewPager.setAdapter(this.adapterViewPager);

        tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setTabMode(TabLayout.MODE_FIXED);
        tabs.setupWithViewPager(viewPager);

        for (int i = 0; i < tabs.getTabCount(); i++) {
            ImageView imageView = new ImageView(getApplicationContext());
            imageView.setImageResource(arrDrawables[i]);
            tabs.getTabAt(i).setCustomView(imageView);
        }

    }

    public CursorAdapter getAdapter(){
        return cursor;
    }

    public static void setAdapter(CursorAdapter adapter){
        cursor = adapter;
    }
}
