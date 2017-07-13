package com.chiri.finalmusicplayer.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.chiri.finalmusicplayer.R;
import com.chiri.finalmusicplayer.adapters.CurrentPlayListAdapter;
import com.chiri.finalmusicplayer.model.Codes;
import com.chiri.finalmusicplayer.model.Song;
import com.chiri.finalmusicplayer.service.MusicService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private boolean iniciando = true;
    private boolean playing = false;
    private int duration;
    private ImageButton playPause, nextSong, previousSong;
    private ImageView albumArt;
    private TextView songName, artistName, totalTime, currentTime;
    private SeekBar seekBar;
    private ListView currentPlayList;
    private CurrentPlayListAdapter adapter;
    private List<Song> songs = new ArrayList<>();

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
    private BroadcastReceiver receiverResult, receiverCurrentPlaylist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        ImageButton libraryButton = (ImageButton) findViewById(R.id.libraryButton);
        libraryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent libraryView = new Intent(MainActivity.this, LibraryActivity.class);
                startActivityForResult(libraryView, CODIGO_LibraryActivity);
            }
        });


    }

    private void init(){
        playPause = (ImageButton)findViewById(R.id.playButton);
        nextSong = (ImageButton)findViewById(R.id.nextSongButton);
        previousSong = (ImageButton)findViewById(R.id.previousSongButton);
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.this.iCallService != null) {
                    if (playing) {
                        playPause.setImageResource(R.drawable.ic_play_arrow);
                        MainActivity.this.iCallService.pause();
                        playing = false;
                    } else {
                        playPause.setImageResource(R.drawable.ic_action_playback_pause);
                        MainActivity.this.iCallService.resume();
                        playing = true;
                    }
                }
            }
        });
        nextSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.iCallService.nextSong();
            }
        });
        previousSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.iCallService.previousSong();
            }
        });
        songName = (TextView) findViewById(R.id.songName);
        artistName = (TextView) findViewById(R.id.artistName);
        albumArt = (ImageView) findViewById(R.id.albumImage);
        totalTime = (TextView)findViewById(R.id.totalTime);
        currentTime = (TextView)findViewById(R.id.currentTime);
        seekBar = (SeekBar)findViewById(R.id.seekBar);

        currentPlayList = (ListView) findViewById(R.id.currentPlayList);
        currentPlayList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MainActivity.this.iCallService.playSelectedSong(position);
            }
        });
        this.adapter = new CurrentPlayListAdapter(getApplicationContext(),this.songs);
        currentPlayList.setAdapter(adapter);
        receiverResult = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Song s = intent.getExtras().getParcelable(Codes.TAG_SONG);
                Log.i("Cancion recibida", s.getSongName());
                songName.setText(s.getSongName());
                albumArt.setImageURI(Uri.parse(s.getAlbumArt()));
                artistName.setText(s.getArtistName());
                duration = (int) (long) s.getDuration();
                updateTime(duration, totalTime);
                setSeekBar(duration);
            }
        };
        receiverCurrentPlaylist = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if( ((ArrayList) intent.getExtras().getParcelableArrayList(Codes.TAG_CURRENT_PLAYLIST)) != null) {
                    MainActivity.this.songs.clear();
                    MainActivity.this.songs.addAll((ArrayList) intent.getExtras().getParcelableArrayList(Codes.TAG_CURRENT_PLAYLIST));
                    Log.i("Result-for-currentPL", MainActivity.this.songs.toString());
                }
            }
        };
    }

    private void setSeekBar(int totalTime) {
        this.seekBar.setMax(totalTime);
        this.seekBar.setProgress(0);
        final Handler mHandler = new Handler();
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int currentPosition = MainActivity.this.iCallService.getCurrentPosition();
                seekBar.setProgress(currentPosition);
                mHandler.postDelayed(this, 1000);
            }
        });
        this.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    MainActivity.this.iCallService.seekTo(progress);
                }
                updateTime(progress, currentTime);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void updateTime(int pos_actual, TextView text){
        String secondsString, minutesString;
        int minutes = pos_actual / (60 * 1000);
        int seconds = (pos_actual / 1000) % 60;

        if(seconds < 10){
            secondsString = "0" + seconds;
        }else{
            secondsString = "" + seconds;
        }
        if(minutes < 10){
            minutesString = "0" + minutes;
        }else{
            minutesString = "" + minutes;
        }
        text.setText(minutesString + ":" + secondsString);
    }

    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CODIGO_LibraryActivity) {
                if (resultCode == Activity.RESULT_OK) {
                    Intent newIntent = new Intent(this,MusicService.class);
                    Bundle newBundle = data.getExtras();
                    Log.i("Code-MainActivity", newBundle.getString(Codes.TAG_TYPE));
                    newIntent.putExtras(newBundle);
                    startService(newIntent);
                    bindService(newIntent,sc,BIND_AUTO_CREATE);
                    if(iniciando){
                        //musicService.play();
                    }
                    if(!playing) {
                        playPause.setImageResource(R.drawable.ic_action_playback_pause);
                        this.playing = true;
                    }
                }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiverResult),
                new IntentFilter(Codes.TAG_SEND_RESULT)
        );
        LocalBroadcastManager.getInstance(this).registerReceiver((receiverCurrentPlaylist),
                new IntentFilter(Codes.TAG_SEND_CURRENT_PLAYLIST)
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver((receiverResult), new IntentFilter(Codes.TAG_SEND_RESULT));
        registerReceiver((receiverCurrentPlaylist), new IntentFilter(Codes.TAG_SEND_CURRENT_PLAYLIST));
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverResult);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverCurrentPlaylist);
        super.onStop();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverResult);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverCurrentPlaylist);
        super.onPause();
    }
}
