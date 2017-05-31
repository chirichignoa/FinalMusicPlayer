package com.chiri.finalmusicplayer.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.chiri.finalmusicplayer.R;
import com.chiri.finalmusicplayer.model.Codes;
import com.chiri.finalmusicplayer.model.Song;
import com.chiri.finalmusicplayer.service.MusicService;

public class MainActivity extends AppCompatActivity {

    private ImageButton libraryButton;
    private boolean playing = false;
    private ImageButton playPause;
    private ImageView albumArt;
    private TextView songName, artistName, totalTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playPause = (ImageButton)findViewById(R.id.playButton);
        songName = (TextView) findViewById(R.id.songName);
        artistName = (TextView) findViewById(R.id.artistName);
        albumArt = (ImageView) findViewById(R.id.albumImage);
        totalTime = (TextView)findViewById(R.id.totalTime);

        libraryButton = (ImageButton)findViewById(R.id.libraryButton);
        libraryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent libraryView = new Intent(MainActivity.this , LibraryActivity.class);
                startActivity(libraryView);
            }
        });

    }

//    Song s = intent.getParcelableExtra(Codes.EXTRA_SONG);
//            Log.i("Cancion recibida", s.getSongName());
//            songName.setText(s.getSongName());
//            albumArt.setImageURI(Uri.parse(s.getAlbumArt()));
//            artistName.setText(s.getArtistName());
//
//              Long duration = s.getDuration();
//    Long minutes = duration / (60 * 1000);
//    Long seconds = (duration / 1000) % 60;
//
//            totalTime.setText(minutes.toString() + ":" + seconds.toString());



}
