package com.chiri.finalmusicplayer.activities;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.chiri.finalmusicplayer.R;
import com.chiri.finalmusicplayer.adapters.AlbumAdapter;
import com.chiri.finalmusicplayer.adapters.PagerAdapter;
import com.chiri.finalmusicplayer.adapters.PlaylistsAdapter;
import com.chiri.finalmusicplayer.adapters.SongAdapter;

public class LibraryActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;

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
        if (checkPermission()) {
            setupTabLayout();
        } else {
            requestPermission(); // Code for permission
        }
    }
    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(LibraryActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(LibraryActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Toast.makeText(LibraryActivity.this, "Leer del almacenamiento permite obtener los archivos de audio del sistema. Por favor conceda este permiso en los ajustes.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(LibraryActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted.");
                    setupTabLayout();
                } else {
                    Log.e("value", "Permission Denied.");
                }
                break;
        }
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
