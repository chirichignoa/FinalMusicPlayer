package com.chiri.finalmusicplayer.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    private static final int CODIGO_LibraryActivity = 1;
    private static final int PERMISSION_REQUEST_CODE = 1;

    private static final String API_TOKEN = "32ccacf88fc879900f65b8784e7cf927";
    private static final String API_URL = "http://api.vagalume.com.br/search.php?";
    private static final String API_PARAMETER_SEPARATOR = "&";
    private static final String API_PARAMETER_ARTIST = "art=";
    private static final String API_PARAMETER_SONG = "mus=";
    private static final String API_PARAMETER_KEY = "apikey=";


    private int duration;
    private ImageButton playPause, nextSong, previousSong, saveButton, stop;
    private ImageView albumArt;
    private TextView songName, artistName, totalTime, currentTime, lyricView;
    private SeekBar seekBar;
    private CurrentPlayListAdapter adapter;
    private ListView currentPlayList;
    private Button lyricButton;

    private static ArrayList<Song> songs = new ArrayList<>();
    private Song playingTrack;

    private View.OnClickListener hiddenItems, restoreItems;

    private MusicService.MusicServiceBinder iCallService = null;
    private MusicService musicService = null;
    private boolean bounded = false;

    View.OnClickListener saveLyric = new View.OnClickListener() {
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
    };

    View.OnClickListener savePlaylist = new View.OnClickListener() {
        String mPlaylistId;

        @Override
        public void onClick(View v) {
            final ContentResolver cr = getApplicationContext().getContentResolver();

            final String date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());

            //Armo un Dialogo para consultar al usuario nombre de la playlist
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Guardar Playlist");
            builder.setMessage("Ingrese nombre de la Playlist");

            // Armo un EditText view para obtener la respuesta del usuario
            final EditText input = new EditText(MainActivity.this);
            input.setText("No-name-PL" + date);
            input.selectAll();
            // Le asigno el EditText al constructor del Diálogo
            builder.setView(input);

            // Guardo la Playlist solo si oprime el boton "Guardar"
            // Agrego un bloque Try-Catch por si el EditText viene vacío, en ese caso se usa el nombre por defecto.
            builder.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    try
                    {
                        createMediaStorePlaylist(input.getText().toString());
                    }
                    catch (Exception e)
                    {
                        createMediaStorePlaylist("No-name-PL" + date);
                    }

                    for (Song song: songs) {
                        long audioId = getAudioID(cr,song.getUri());
                        if (audioId!=-1) {
                            addSongToPlaylist(cr, audioId, Long.parseLong(mPlaylistId));
                        }
                    }
                    Toast.makeText(getApplicationContext(),"Se ha guardado exitosamente la playlist", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancelar", null);
            builder.create().show();
        }

        private long getAudioID(ContentResolver cr, String data) {
            long id = -1;
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String selection = MediaStore.Audio.Media.DATA;
            String[] selectionArgs = {data};
            String[] projection = {MediaStore.Audio.Media._ID,};
            String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

            Cursor cursor = cr.query(uri, projection, selection + "=?", selectionArgs, sortOrder);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int idIndex = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                    id = Long.parseLong(cursor.getString(idIndex));
                }
            }
            return id;
        }

        private void createMediaStorePlaylist(String playlistName) {
            ContentValues mInserts = new ContentValues();
            mInserts.put(MediaStore.Audio.Playlists.NAME, playlistName);
            mInserts.put(MediaStore.Audio.Playlists.DATE_ADDED, System.currentTimeMillis());
            mInserts.put(MediaStore.Audio.Playlists.DATE_MODIFIED, System.currentTimeMillis());

            Uri mUri = getApplicationContext().getContentResolver().insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, mInserts);
            if (mUri != null) {
                String[] PROJECTION_PLAYLIST = new String[] {
                        MediaStore.Audio.Playlists._ID,
                        MediaStore.Audio.Playlists.NAME,
                        MediaStore.Audio.Playlists.DATA
                };
                Cursor c = getApplicationContext().getContentResolver().query(mUri, PROJECTION_PLAYLIST, null, null, null);
                if (c != null) {
                /* Save the newly created ID so it can be selected. Names are allowed to be duplicated,
                 * but IDs can never be. */
                    c.moveToFirst();
                    mPlaylistId = "" + c.getLong(c.getColumnIndex(Audio.Playlists._ID));
                    c.close();
                }

            }

        }

        private void addSongToPlaylist(ContentResolver resolver, long audioId, long playlistId) {

            String[] cols = new String[] {
                    "count(*)"
            };
            Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
            Cursor cur = resolver.query(uri, cols, null, null, null);
            cur.moveToFirst();
            final int base = cur.getInt(0);
            cur.close();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, base + audioId);
            values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, audioId);
            resolver.insert(uri, values);
        }
    };


    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iCallService = (MusicService.MusicServiceBinder) service;
            musicService = iCallService.getService();
            bounded = true;
            iCallService.getResult();
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
        Log.d("Lifecycle", "onCreate MainActivity Bounded: "+bounded);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        if (checkPermission()) {
            init();
        } else {
            requestPermission(); // Code for permission
        }

        //Chequeo si hay un Service corriendo y necesito bindearlo al iniciar de nuevo la App
        Intent checkService = new Intent(this,MusicService.class);
        bindService(checkService,sc,0);
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
        stop = (ImageButton) findViewById(R.id.stopButton);
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.this.iCallService != null) {
                    if (MainActivity.this.iCallService.isPaused()) {
                        MainActivity.this.iCallService.resume();
                        playPause.setImageResource(R.drawable.ic_action_playback_pause);
                    } else if (MainActivity.this.iCallService.isPlaying())  {
                        MainActivity.this.iCallService.pause();
                        playPause.setImageResource(R.drawable.ic_play_arrow);
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
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
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
                if (bounded) {
                    MainActivity.this.iCallService.playSelectedSong(position);
                }
                else{
                    playCurrentPlayList(position);
                }
            }
        });

        this.adapter = new CurrentPlayListAdapter(getApplicationContext(),this.songs);
        currentPlayList.setAdapter(adapter);

        lyricView = (TextView) findViewById(R.id.lyricView);
        lyricView.setVisibility(View.INVISIBLE);
        lyricView.setMovementMethod(new ScrollingMovementMethod());
        lyricButton = (Button)findViewById(R.id.lyricButton);
        saveButton = (ImageButton)findViewById(R.id.saveButton);

        //saveButton.setVisibility(View.INVISIBLE); //buscar en el codigo cuando se hace visible y poner el saveLyric

        saveButton.setOnClickListener(this.savePlaylist);

        //saveButton.setOnClickListener(saveLyric);

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
               // MainActivity.this.saveButton.setVisibility(View.INVISIBLE);
                saveButton.setOnClickListener(savePlaylist);
                MainActivity.this.lyricButton.setText(R.string.lyricButton);
                MainActivity.this.lyricButton.setOnClickListener(MainActivity.this.hiddenItems);
            }
        };
    }

    private void playCurrentPlayList(int position){

        Intent newIntent = new Intent(this,MusicService.class);
        newIntent.putExtra(Codes.TAG_TYPE,Codes.TAG_CURRENT_PLAYLIST);
        newIntent.putExtra(Codes.TAG_PLAYLIST,songs);
        newIntent.putExtra(Codes.TAG_POSITION,position);
        startService(newIntent);
        bindService(newIntent,sc,0);
    }

    private void stop() {

        Intent service = new Intent(this,MusicService.class);
        updateTime(0,totalTime);
        setSeekBar(0);
        playPause.setImageResource(R.drawable.ic_play_arrow);

        if (bounded) {
            iCallService.stop();
        }

        stopService(service);
    }


    private void setSeekBar(int totalTime) {
        this.seekBar.setMax(totalTime);
        this.seekBar.setProgress(0);
        final Handler mHandler = new Handler();
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (MainActivity.this.iCallService != null) {
                    int currentPosition = MainActivity.this.iCallService.getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                    mHandler.postDelayed(this, 1000);
                }
            }
        });
        this.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (MainActivity.this.iCallService != null) {
                        MainActivity.this.iCallService.seekTo(progress);
                    }
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
                    bindService(newIntent,sc,0);
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
        Log.d("Lifecycle", "onResume - Bounded: " + bounded);
        super.onResume();

        registerReceiver(playingTrackReceiver, new IntentFilter(Codes.TAG_SEND_RESULT));

        // Me bindeo al Service sólo si está corriendo:
        Intent checkService = new Intent(this,MusicService.class);
        bindService(checkService,sc,0);
    }

    private BroadcastReceiver playingTrackReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            playingTrack = intent.getExtras().getParcelable(Codes.TAG_SONG);
            ArrayList<Song> currentPlayList = intent.getParcelableArrayListExtra(Codes.TAG_PLAYLIST);

            Log.d("LISTA RECIBIDA",currentPlayList.toString());
            songs.clear();
            songs.addAll(currentPlayList);
            adapter.notifyDataSetChanged();

            Log.d("CANCION RECIBIDA",playingTrack.toString());
            songName.setText(playingTrack.getSongName());
            if (playingTrack.getAlbumArt() != null) {
                albumArt.setImageURI(Uri.parse(playingTrack.getAlbumArt()));
            }
            artistName.setText(playingTrack.getArtistName());
            duration = (int) (long) playingTrack.getDuration();
            if (iCallService != null){
                if (iCallService.isPlaying()) {
                    if (!(iCallService.isPaused())) {
                        playPause.setImageResource(R.drawable.ic_action_playback_pause);
                    }
                }
            }
            updateTime(duration, totalTime);
            setSeekBar(duration);
        }
        };


    @Override
    protected void onRestart() {
        Log.d("Lifecycle", "OnReStart - Bounded: " + bounded);
        super.onRestart();
    }

    @Override
    protected void onStop() {
        Log.d("Lifecycle", "OnStop - Bounded: " + bounded);
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d("Lifecycle", "OnPause - Bounded: " + bounded);

        if(bounded) {
            unbindService(sc);
        }
        unregisterReceiver(playingTrackReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d("Lifecycle", "OnDestroy - Bounded: " + bounded);

        super.onDestroy();
    }

    private void searchLyric() {
        if(this.iCallService != null && this.iCallService.isPlaying()){
            this.setItemsStatus(View.INVISIBLE);
            this.lyricView.setVisibility(View.VISIBLE);
            //this.saveButton.setVisibility(View.VISIBLE);
            this.saveButton.setOnClickListener(saveLyric);
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