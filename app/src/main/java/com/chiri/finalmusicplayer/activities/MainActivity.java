package com.chiri.finalmusicplayer.activities;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.chiri.finalmusicplayer.R;
import com.chiri.finalmusicplayer.adapters.CurrentPlayListAdapter;
import com.chiri.finalmusicplayer.model.Codes;
import com.chiri.finalmusicplayer.model.Song;
import com.chiri.finalmusicplayer.service.MusicService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    private boolean iniciando = true;
    private boolean playing = false;
    private int duration;
    private ImageButton playPause, nextSong, previousSong, saveButton;
    private ImageView albumArt;
    private TextView songName, artistName, totalTime, currentTime, lyricView;
    private SeekBar seekBar;
    private ListView currentPlayList;
    private CurrentPlayListAdapter adapter;
    private List<Song> songs = new ArrayList<>();
    private Button lyricButton;

    private boolean bounded = false;

    private View.OnClickListener hiddenItems, restoreItems;

    private static final int CODIGO_LibraryActivity = 1;

    private static final String API_TOKEN = "32ccacf88fc879900f65b8784e7cf927";
    private static final String API_URL = "http://api.vagalume.com.br/search.php?";
    private static final String API_PARAMETER_SEPARATOR = "&";
    private static final String API_PARAMETER_ARTIST = "art=";
    private static final String API_PARAMETER_SONG = "mus=";
    private static final String API_PARAMETER_KEY = "apikey=";

    private MusicService.MusicServiceBinder iCallService;
    private MusicService musicService;

    private BroadcastReceiver receiverResult, receiverCurrentPlaylist;

    private static final int PERMISSION_REQUEST_CODE = 1;

    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iCallService = (MusicService.MusicServiceBinder) service;
            musicService = iCallService.getService();
            bounded = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            iCallService = null;
            musicService = null;
            bounded = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Lifecycle", "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        if (checkPermission()) {
            init();
        } else {
            requestPermission(); // Code for permission
        }
    }

    private void init(){
        ImageButton libraryButton = (ImageButton) findViewById(R.id.libraryButton);
        libraryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent libraryView = new Intent(MainActivity.this, LibraryActivity.class);
                startActivityForResult(libraryView, CODIGO_LibraryActivity);
            }
        });
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
            public void onClick(View v)
            {
                if (MainActivity.this.iCallService != null) {
                    MainActivity.this.iCallService.nextSong();
                }
            }
        });
        previousSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (MainActivity.this.iCallService != null) {
                    MainActivity.this.iCallService.previousSong();
                }
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
                Log.i("Cancion recibida:", s.getSongName());
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
                //if( ((ArrayList) intent.getExtras().getParcelableArrayList(Codes.TAG_CURRENT_PLAYLIST)) != null) {
                    MainActivity.this.songs.clear();
                    MainActivity.this.songs.addAll((ArrayList) intent.getExtras().getParcelableArrayList(Codes.TAG_CURRENT_PLAYLIST));
                    Log.i("Result-for-currentPL", MainActivity.this.songs.toString());
                //}
            }
        };
        lyricView = (TextView) findViewById(R.id.lyricView);
        lyricView.setVisibility(View.INVISIBLE);
        lyricView.setMovementMethod(new ScrollingMovementMethod());
        lyricButton = (Button)findViewById(R.id.lyricButton);
        saveButton = (ImageButton)findViewById(R.id.saveButton);
        saveButton.setVisibility(View.INVISIBLE);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName = MainActivity.this.songName.getText().toString().replace(" ","-") + ".txt" ;
                //File file = new File(getApplicationContext().getFilesDir(), filename);
                File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Text/" );
                if (!dir.exists())
                {
                    if(!dir.mkdirs()){
                        Log.e("ALERT","could not create the directories");
                    }
                }
                final File file = new File(dir, fileName);
                try {
                    if (!file.exists())
                    {
                        file.createNewFile();
                    }
                    FileOutputStream fOut = new FileOutputStream(file);
                    Log.d("File", "File created with name: " + fileName);
                    Log.d("File", "Writing file");
                    // myOutWriter.append(MainActivity.this.lyricView.getText().toString().getBytes());
                    /*myOutWriter.write(MainActivity.this.lyricView.getText().toString());
                    myOutWriter.close();*/
                    fOut.write(MainActivity.this.lyricView.getText().toString().getBytes());
                    fOut.close();
                    Log.d("File", "Closing file");
                    Toast.makeText(MainActivity.this.getApplicationContext(),"Se guardo exitosamente la letra, en el directorio: " + file.toString(), Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Log.d("File", e.getMessage());
                }
            }
        });

        this.hiddenItems = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.searchLyric();
            }
        };
        lyricButton.setOnClickListener(this.hiddenItems);
        this.restoreItems = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.setItemsStatus(View.VISIBLE);
                MainActivity.this.lyricView.setVisibility(View.INVISIBLE);
                MainActivity.this.saveButton.setVisibility(View.INVISIBLE);
                MainActivity.this.lyricButton.setText(R.string.lyricButton);
                MainActivity.this.lyricButton.setOnClickListener(MainActivity.this.hiddenItems);
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
                    if(!playing) {
                        playPause.setImageResource(R.drawable.ic_action_playback_pause);
                        this.playing = true;
                    }
                    //Intent intent = new Intent(this,MusicService.class);
                    //intent.putExtra(Codes.TAG_TYPE,Codes.TAG_SEND_RESULT);
                }
        }
    }

    @Override
    protected void onStart() {
        Log.d("Lifecycle", "onStart");
        super.onStart();

        Log.d("Lifecycle", "OnStart - Bounded: " + bounded);

    }

    @Override
    protected void onResume() {
        Log.d("Lifecycle", "onResume");
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver((receiverResult),
                new IntentFilter(Codes.TAG_SEND_RESULT)
        );
        LocalBroadcastManager.getInstance(this).registerReceiver((receiverCurrentPlaylist),
                new IntentFilter(Codes.TAG_SEND_CURRENT_PLAYLIST)
        );
        /*if(!bounded) {
            Intent intent = new Intent(this,MusicService.class);
            intent.putExtra(Codes.TAG_TYPE,Codes.TAG_SEND_RESULT);
            startService(intent);
            bindService(intent,sc,BIND_AUTO_CREATE);
            bounded = true;
        }*/
    }

    @Override
    protected void onRestart() {
        Log.d("Lifecycle", "onRestart");
        super.onRestart();

//        registerReceiver((receiverCurrentPlaylist), new IntentFilter(Codes.TAG_SEND_CURRENT_PLAYLIST));
//        registerReceiver((receiverResult), new IntentFilter(Codes.TAG_SEND_RESULT));
        /*
        if(!bounded) {
            Intent intent = new Intent(this,MusicService.class);
            intent.putExtra(Codes.TAG_TYPE,Codes.TAG_SEND_RESULT);
            startService(intent);

        }
        */
        if(!bounded) {
            Intent intent = new Intent(this,MusicService.class);
            bindService(intent,sc,BIND_AUTO_CREATE);
            bounded = true;
        }

    }

    @Override
    protected void onStop() {
        Log.d("Lifecycle", "onStop");
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverResult);
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverCurrentPlaylist);
        if(bounded) {
            unbindService(sc);
            bounded = false;
        }
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d("Lifecycle", "onPause");
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverResult);
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverCurrentPlaylist);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d("Lifecycle", "onDestroy");

        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverResult);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverCurrentPlaylist);
        if(bounded) {
            unbindService(sc);
            bounded = false;
        }
        super.onDestroy();
    }

    private void searchLyric() {
        if(playing){
            this.setItemsStatus(View.INVISIBLE);
            this.lyricView.setVisibility(View.VISIBLE);
            this.saveButton.setVisibility(View.VISIBLE);
            this.lyricButton.setText(R.string.backButton);
            this.lyricButton.setOnClickListener(this.restoreItems);
            String artistName = this.artistName.getText().toString().replace(" ","-");
            String songName = this.songName.getText().toString().replace(" ","-");

            StringBuffer url = new StringBuffer();
                url.append(API_URL)
                .append(API_PARAMETER_ARTIST)
                .append(artistName)
                .append(API_PARAMETER_SEPARATOR)
                .append(API_PARAMETER_SONG)
                .append(songName)
                .append(API_PARAMETER_SEPARATOR)
                .append(API_PARAMETER_KEY)
                .append(API_TOKEN);

            // Instantiate the RequestQueue.
            RequestQueue queue = Volley.newRequestQueue(this);

            Log.d("URL", url.toString());
            // Request a string response from the provided URL.
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url.toString(), null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            lyricView.setText(parseJSON(response));
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    lyricView.setText(R.string.lyricError);
                }
            });
            // Add the request to the RequestQueue.
            queue.add(jsonObjectRequest);
        } else {
            Toast.makeText(getApplicationContext(), R.string.lyricMessage,Toast.LENGTH_LONG).show();
        }
    }

    private String parseJSON(JSONObject response)  {
        JSONArray array = null;
        try {
            array = response.getJSONArray("mus");
            if(array == null) {
                return getApplicationContext().getString(R.string.lyricNotFound);
            } else {
                    return response.getJSONArray("mus").getJSONObject(0).getString("text");
            }
        }
        catch (JSONException e) {
            return getApplicationContext().getString(R.string.lyricNotFound);
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, INTERNET);
        int result2 = ContextCompat.checkSelfPermission(MainActivity.this, WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, INTERNET)) {
            Toast.makeText(MainActivity.this, "Usar internet permite obtener letras de las canciones. Por favor conceda este permiso en los ajustes.", Toast.LENGTH_LONG).show();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(MainActivity.this, "Usar la escritura permite guardar letras de las canciones. Por favor conceda este permiso en los ajustes.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{INTERNET, WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted.");
                    init();
                } else {
                    Log.e("value", "Permission Denied.");
                }
                break;
        }
    }

    private void setItemsStatus(int visibility) {
        this.playPause.setVisibility(visibility);
        this.nextSong.setVisibility(visibility);
        this.previousSong.setVisibility(visibility);
        this.totalTime.setVisibility(visibility);
        this.currentTime.setVisibility(visibility);
        this.seekBar.setVisibility(visibility);;
        this.currentPlayList.setVisibility(visibility);
    }


}
