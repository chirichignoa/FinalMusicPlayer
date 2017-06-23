package com.chiri.finalmusicplayer.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chiri.finalmusicplayer.R;
import com.chiri.finalmusicplayer.adapters.CurrentPlayListAdapter;
import com.chiri.finalmusicplayer.model.Codes;
import com.chiri.finalmusicplayer.model.Song;
import com.chiri.finalmusicplayer.service.MusicService;

import static com.chiri.finalmusicplayer.model.Codes.TAG_SONG_TITLE;

public class MainActivity extends AppCompatActivity {

    private boolean playing = false;
    private int minutes, seconds, duration;
    private ImageButton playPause;
    private ImageView albumArt;
    private TextView songName, artistName, totalTime, currentTime;
    private SeekBar seekBar;
    private final Handler mHandler = new Handler();
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

        init();

        ListView currentPlayList = (ListView) findViewById(R.id.currentPlayList);
        CursorAdapter currentPlayListAdapter = new CurrentPlayListAdapter(this, null, false);
        currentPlayList.setAdapter(currentPlayListAdapter);

        ImageButton libraryButton = (ImageButton) findViewById(R.id.libraryButton);
        libraryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent libraryView = new Intent(MainActivity.this, LibraryActivity.class);
                startActivityForResult(libraryView, CODIGO_LibraryActivity);
            }
        });

        registerReceiver(receiver, new IntentFilter(Codes.TAG_SEND_RESULT));


    }

    private void init(){
        playPause = (ImageButton)findViewById(R.id.playButton);
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playing){
                    playPause.setImageResource(R.drawable.ic_play_arrow);
                    MainActivity.this.iCallService.pause();
                    playing = false;
                } else {
                    playPause.setImageResource(R.drawable.ic_action_playback_pause);
                    MainActivity.this.iCallService.resume();
                    playing = true;
                }
            }
        });
        songName = (TextView) findViewById(R.id.songName);
        artistName = (TextView) findViewById(R.id.artistName);
        albumArt = (ImageView) findViewById(R.id.albumImage);
        totalTime = (TextView)findViewById(R.id.totalTime);
        currentTime = (TextView)findViewById(R.id.currentTime);
        seekBar = (SeekBar)findViewById(R.id.seekBar);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Song s = intent.getExtras().getParcelable(Codes.TAG_SONG);
                Log.i("Cancion recibida", s.getSongName());
                songName.setText(s.getSongName());
                albumArt.setImageURI(Uri.parse(s.getAlbumArt()));
                artistName.setText(s.getArtistName());

                duration = (int) (long) s.getDuration();
                minutes = duration / (60 * 1000);
                seconds = (duration / 1000) % 60;
                totalTime.setText(Integer.toString(minutes) + ":" + Integer.toString(seconds));
                setSeekBar(duration);
            }
        };
    }

    private void setSeekBar(int totalTime) {
        minutes = 0;
        seconds = 0;
        this.seekBar.setMax(totalTime);
        this.seekBar.setProgress(0);
        final Handler mHandler = new Handler();
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int currentPosition = MainActivity.this.iCallService.getCurrentPosition();
                seekBar.setProgress(currentPosition);
                updateTime();
                mHandler.postDelayed(this, 1000);
            }
        });
        this.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                MainActivity.this.iCallService.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void updateTime(){
        String secondsString, minutesString;
        if(seconds + 1 < 60){
            seconds += 1;
        } else {
            minutes +=1;
            seconds = 0;
        }
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
        currentTime.setText(minutesString + ":" + secondsString);
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
                    newIntent.putExtras(newBundle);
                    bindService(newIntent,sc,BIND_AUTO_CREATE);
                    playPause.setImageResource(R.drawable.ic_action_playback_pause);
                    this.playing = true;
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
