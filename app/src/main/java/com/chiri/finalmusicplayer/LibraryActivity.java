package com.chiri.finalmusicplayer;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

public class LibraryActivity extends AppCompatActivity {
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);


        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);


        tabs.addTab(tabs.newTab().setIcon(R.drawable.ic_music_note));
        tabs.addTab(tabs.newTab().setIcon(R.drawable.ic_album));
        tabs.addTab(tabs.newTab().setIcon(R.drawable.ic_playlist_play));
    }

}
