package com.chiri.finalmusicplayer;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.chiri.finalmusicplayer.adapters.AlbumAdapter;
import com.chiri.finalmusicplayer.adapters.PagerAdapter;
import com.chiri.finalmusicplayer.adapters.PlaylistsAdapter;
import com.chiri.finalmusicplayer.adapters.SongAdapter;

public class LibraryActivity extends AppCompatActivity {

    private CursorAdapter songAdapter;
    private CursorAdapter albumAdapter;
    private CursorAdapter playlistsAdapter;
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

    public CursorAdapter getSongAdapter() {
        return songAdapter;
    }

    public void setSongAdapter(SongAdapter songAdapter) {
        this.songAdapter = songAdapter;
    }

    public CursorAdapter getAlbumAdapter() {
        return albumAdapter;
    }

    public void setAlbumAdapter(AlbumAdapter albumAdapter) {
        this.albumAdapter = albumAdapter;
    }

    public CursorAdapter getPlaylistsAdapter() {
        return playlistsAdapter;
    }

    public void setPlaylistsAdapter(PlaylistsAdapter playlistsAdapter) {
        this.playlistsAdapter = playlistsAdapter;
    }

}
