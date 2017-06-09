package com.chiri.finalmusicplayer.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.chiri.finalmusicplayer.R;
import com.chiri.finalmusicplayer.adapters.CurrentPlayListAdapter;
import com.chiri.finalmusicplayer.model.Codes;
import com.chiri.finalmusicplayer.model.Song;
import com.chiri.finalmusicplayer.service.MusicService;

import static com.chiri.finalmusicplayer.model.Codes.TAG_SONG_TITLE;

public class MainActivity extends AppCompatActivity {

    private ImageButton libraryButton;
    private boolean playing = false;
    private ImageButton playPause;
    private ImageView albumArt;
    private TextView songName, artistName, totalTime;
    private ListView currentPlayList;
    private CursorAdapter currentPlayListAdapter;
    private static final int CODIGO_LibraryActivity = 1;
    private MusicService.MusicServiceBinder iCallService;
    private MusicService musicService;
    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iCallService = (MusicService.MusicServiceBinder) service;
            musicService = iCallService.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            iCallService = null;
        }
    };

    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playPause = (ImageButton)findViewById(R.id.playButton);
        songName = (TextView) findViewById(R.id.songName);
        artistName = (TextView) findViewById(R.id.artistName);
        albumArt = (ImageView) findViewById(R.id.albumImage);
        totalTime = (TextView)findViewById(R.id.totalTime);

        currentPlayList = (ListView) findViewById(R.id.currentPlayList);
        currentPlayListAdapter = new CurrentPlayListAdapter(this,null,false);
        currentPlayList.setAdapter(currentPlayListAdapter);

        libraryButton = (ImageButton)findViewById(R.id.libraryButton);
        libraryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent libraryView = new Intent(MainActivity.this, LibraryActivity.class);
                //startActivity(libraryView);
                startActivityForResult(libraryView, CODIGO_LibraryActivity);
            }
        });

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
//                Song s = intent.getParcelableExtra(Codes.TAG_SONG);
                Song s = intent.getExtras().getParcelable(Codes.TAG_SONG);
                Log.i("Cancion recibida", s.getSongName());
                songName.setText(s.getSongName());
                albumArt.setImageURI(Uri.parse(s.getAlbumArt()));
                artistName.setText(s.getArtistName());

                Long duration = s.getDuration();
                Long minutes = duration / (60 * 1000);
                Long seconds = (duration / 1000) % 60;

                totalTime.setText(minutes.toString() + ":" + seconds.toString());
            }
        };
        registerReceiver(receiver, new IntentFilter(Codes.TAG_SEND_RESULT));
    }

    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CODIGO_LibraryActivity) {
                if (resultCode == Activity.RESULT_OK) {
                    String song_name = data.getStringExtra(TAG_SONG_TITLE);
                    Toast.makeText(MainActivity.this, song_name, Toast.LENGTH_LONG).show();
                    Intent newIntent = new Intent(this,MusicService.class);
                    Bundle newBundle = data.getExtras();
                    newIntent.putExtras(newBundle);
                    bindService(newIntent,sc,BIND_AUTO_CREATE);
                }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(Codes.TAG_SEND_RESULT)
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver((receiver), new IntentFilter(Codes.TAG_SEND_RESULT));
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }
}
