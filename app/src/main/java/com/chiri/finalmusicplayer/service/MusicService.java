package com.chiri.finalmusicplayer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.chiri.finalmusicplayer.PlayerWidget;
import com.chiri.finalmusicplayer.R;
import com.chiri.finalmusicplayer.activities.MainActivity;
import com.chiri.finalmusicplayer.model.Codes;
import com.chiri.finalmusicplayer.model.Song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chiri on 26/05/17.
 */

public class MusicService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    public final static String TAG = MusicService.class.getCanonicalName();

    private static MediaPlayer mediaPlayer;
    private static String artistName, songName, albumArt, albumName, playlistName;
    private static ArrayList<Song> songs = new ArrayList<>();
    private static int playingTrack = 0; //Será 0 si no hay ningún PlayingTrack reproduciendo (aunque esté en pausa)
    private static boolean isPlaying = false; //Será True si MediaPlayer está ejecutando (Aunque esté el PlayingTrack en pausa)
    private static boolean isPaused = false; //Será True si isPlaying es true y además, el playingTrack está en puasa.

    public MusicService() {
    }

    @Override
    public void onCreate() {
        Log.d("Lifecycle", "SERVICE: onCreate");
        super.onCreate();
        initMediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Lifecycle", "SERVICE: onStartCommand");
        decodeIntent(intent);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("Lifecycle", "SERVICE: onBind");
        return new MusicServiceBinder();
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.reset();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setVolume(90, 90);
    }

    private void getSongsOfBundle(Bundle bundle){
        songName = bundle.getString(Codes.TAG_SONG_TITLE);
        artistName = bundle.getString(Codes.TAG_ARTIST);
        albumArt = bundle.getString(Codes.TAG_ALBUMART);
    }

    private void decodeIntent(Intent intent){
        final Bundle bundle = intent.getExtras();
        SongLoader sl = new SongLoader();
        switch (bundle.getString(Codes.TAG_TYPE,"NULL")){
            case Codes.TAG_SONG: {
                MusicService.this.stop();
                songs.clear();
                playingTrack = 0;

                getSongsOfBundle(bundle);
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] projection = { MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.DATA};
                String selection = "IS_MUSIC != 0 AND " + MediaStore.Audio.Media.TITLE + "=? AND "
                        + MediaStore.Audio.Media.ARTIST + "=?";
                String[] selectionArgs = {songName, artistName};
                sl.execute(uri,projection,selection,selectionArgs, false);
            }
            break;
            case Codes.TAG_ALBUM: {
                MusicService.this.stop();
                songs.clear();
                playingTrack = 0;

                albumName = bundle.getString(Codes.TAG_ALBUM_TITLE);
                artistName = bundle.getString(Codes.TAG_ALBUM_ARTIST);
                albumArt = bundle.getString(Codes.TAG_ALBUMART);
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] projection = {
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.ALBUM,
                };
                String selection = "IS_MUSIC != 0 AND ALBUM =? ";
                String[] selectionArgs = {albumName};
                sl.execute(uri,projection,selection,selectionArgs,false);
            }
            break;
            case Codes.TAG_PLAYLIST: {
                MusicService.this.stop();
                songs.clear();
                playingTrack = 0;

                playlistName = bundle.getString(Codes.TAG_PLAYLIST_NAME);
                Long playListID = getPlayListID(playlistName);
                Log.d(TAG, playListID.toString());
                if(playListID != -1L ) {
                    Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external",playListID);
                    String[] projection = {
                            MediaStore.Audio.Playlists.Members.AUDIO_ID,
                            MediaStore.Audio.Playlists.Members.TITLE,
                            MediaStore.Audio.Playlists.Members.ARTIST,
                            MediaStore.Audio.Playlists.Members.DURATION,
                            MediaStore.Audio.Playlists.Members.ALBUM,
                            MediaStore.Audio.Playlists.Members.DATA,
                            MediaStore.Audio.Playlists.Members._ID
                    };

                    String selection = MediaStore.Audio.Media.IS_MUSIC +" != 0 ";
                    SongLoaderFromPlaylist slfp = new SongLoaderFromPlaylist();
                    slfp.execute(uri, projection, selection, null);
                }
            }
            break;
            case Codes.TAG_ADD_SONG_QUEUE: {
                songName = bundle.getString(Codes.TAG_SONG_TITLE);
                artistName = bundle.getString(Codes.TAG_ARTIST);
                albumArt = bundle.getString(Codes.TAG_ALBUMART);
                Log.i("DoInBack", "Anadiendo a cola " + songName);

                Log.d("Selection-Song", songName +" - "+ artistName);
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] projection = {MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.DATA};
                String selection = "IS_MUSIC != 0 AND " + MediaStore.Audio.Media.TITLE + "=? AND "
                        + MediaStore.Audio.Media.ARTIST + "=?";
                String[] selectionArgs = {songName, artistName};
                sl.execute(uri, projection, selection, selectionArgs, true);
            }
            break;
            case Codes.TAG_ADD_ALBUM_QUEUE: {
                albumName = bundle.getString(Codes.TAG_ALBUM_TITLE);
                artistName = bundle.getString(Codes.TAG_ALBUM_ARTIST);
                albumArt = bundle.getString(Codes.TAG_ALBUMART);
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] projection = {
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.ALBUM,
                };
                String selection = "IS_MUSIC != 0 AND ALBUM =? ";
                String[] selectionArgs = {albumName};
                sl.execute(uri,projection,selection,selectionArgs,true);
            }
            break;
            case Codes.TAG_ACTION:
                getForegroundAction(intent);
                break;
            case Codes.TAG_CURRENT_PLAYLIST: {
                MusicService.this.stop();
                songs.clear();
                playingTrack = 0;

                int position = intent.getExtras().getInt(Codes.TAG_POSITION);
                ArrayList<Song> currentPlayList = intent.getParcelableArrayListExtra(Codes.TAG_PLAYLIST);
                songs.addAll(currentPlayList);
                playSelectedSong(position);
            }
            break;
            default:
                break;
        }
    }

    public void play() {
        try{
            Log.i("Songs", Integer.toString(songs.size()));
            Uri uri = Uri.parse(songs.get(playingTrack).getUri());
            mediaPlayer.setDataSource(getApplicationContext(),uri);
            Log.i("Media Player","Setting path: "+uri.toString());
            mediaPlayer.prepareAsync();
            this.startForeground();
        }
        catch(Exception e){
            Log.e("Media Player Error", "Error setting data source", e);
        }
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.i("Player Info","Empieza a reproducir");
        Log.d("Lifecycle", "SERVICE: onPrepared");
        mediaPlayer.start();
        isPlaying = true;
        isPaused = false;

        sendResult();
        updateWidget();
    }

    private void sendResult() {
        if(songs.size() > 0) {
            Intent intent = new Intent(Codes.TAG_SEND_RESULT);
            Song s = songs.get(playingTrack);
            Log.d("SONG ANTES DE PUT", s.toString());
            Log.d("LISTA ANTES DE PUT", songs.toString());
            intent.putExtra(Codes.TAG_SONG, s);
            intent.putExtra(Codes.TAG_PLAYLIST, songs);
            sendBroadcast(intent);
            Log.d("RECEIVER", "SEND BROADCAST");
        }
    }


    public void updateWidget() {
        if(songs.size() > 0) {
            Intent intent = new Intent("android.appwidget.action.APPWIDGET_UPDATE");
            intent.putExtra(Codes.TAG_SONG, songs.get(playingTrack));
            AppWidgetManager widgetManager =
                    AppWidgetManager.getInstance(this.getBaseContext());

            ComponentName thisWidget = new ComponentName(getApplicationContext(),
                    PlayerWidget.class);

            int[] allWidgetIds = widgetManager.getAppWidgetIds(thisWidget);
            if (allWidgetIds.length > 0){
                Log.d("Widget", "Actualizando widget id: " + allWidgetIds[0]);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, allWidgetIds[0]);
                PlayerWidget.setCurrentSong(songs.get(playingTrack));
                PlayerWidget.updateAppWidget(this.getBaseContext(),widgetManager,allWidgetIds[0]);
            }
        }
    }

    public void getForegroundAction(Intent intent) {
        Log.d("ACTION_PENDING_INTENT: ",intent.getStringExtra(Codes.TAG_ACTION));

        if (intent.getStringExtra(Codes.TAG_ACTION).equals(Codes.ACTION_PREVIOUS)) {
            this.previousSong();
        } else if (intent.getStringExtra(Codes.TAG_ACTION).equals(Codes.ACTION_PLAYPAUSE)) {
            if(this.isPaused()) {
                this.resume();
            } else {
                this.pause();
            }
        } else if (intent.getStringExtra(Codes.TAG_ACTION).equals(Codes.ACTION_NEXT)) {
            this.nextSong();
        }
    }

    public void stop() {
        Log.i("Player Info","Paro");
        isPlaying = false;
        isPaused = false;
        playingTrack = 0;
        mediaPlayer.stop();
        mediaPlayer.reset();
    }

    public void pause() {
        Log.i("Player Info","Esta en pausa");
        isPaused = true;
        mediaPlayer.pause();
    }

    public void resume(){
        Log.i("Player Info","Resumiendo");
        mediaPlayer.start();
        isPaused = false;
    }

    public void nextSong() {
        Log.i("Player Info","Cancion siguiente");
        if(playingTrack+1 < songs.size()){
            playingTrack += 1;
            mediaPlayer.reset();
            this.play();
        }
    }

    public void previousSong() {
        Log.i("Player Info","Cancion anterior");
        if(playingTrack-1 >= 0){
            playingTrack -= 1;
            mediaPlayer.reset();
            this.play();
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public boolean isPaused() {
        return isPaused;
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        Log.e("Player Error","Fallo de reproductor");
        return true;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.i("Player Info","Termino la cancion numero: "+ playingTrack);
        Log.d("Lifecycle", "SERVICE: onCompletion");
        if(playingTrack+1 == songs.size()) {
            stopSelf();
        }
        else {
            this.nextSong();
        }
    }

    public void playSelectedSong(int position) {
        if(playingTrack != position ) {
            playingTrack = position;
            Log.i("PlayingTrack", Integer.toString(playingTrack));
            mediaPlayer.reset();
            this.play();
        }
    }

    public List getPlaylist(){
        return songs;
    }

    public int getCurrentPosition() {
        return MusicService.mediaPlayer.getCurrentPosition();
    }

    public void seekTo(int position) {
        MusicService.mediaPlayer.seekTo(position);
    }


    private void startForeground(){
        Log.d("Lifecycle", "SERVICE: startForeground");

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_launcher);

        Intent previousIntent = new Intent(this, MusicService.class);
        previousIntent.putExtra("numero","Int 1");
        previousIntent.putExtra(Codes.TAG_TYPE,Codes.TAG_ACTION);
        previousIntent.putExtra(Codes.TAG_ACTION,"PREVIOUS");
        PendingIntent prevPendingIntent = PendingIntent.getService(this, 1,
                previousIntent, 0);

        Intent pauseIntent = new Intent(this, MusicService.class);
        pauseIntent.putExtra("numero","Int 2");
        pauseIntent.putExtra(Codes.TAG_TYPE,Codes.TAG_ACTION);
        pauseIntent.putExtra(Codes.TAG_ACTION,"PLAYPAUSE");
        PendingIntent pausePendingIntent = PendingIntent.getService(this, 2,
                pauseIntent, 0);

        Intent nextIntent = new Intent(this, MusicService.class);
        nextIntent.putExtra("numero","Int 3");
        nextIntent.putExtra(Codes.TAG_TYPE,Codes.TAG_ACTION);
        nextIntent.putExtra(Codes.TAG_ACTION,"NEXT");
        PendingIntent nextPendingIntent = PendingIntent.getService(this, 3,
                nextIntent, 0);

        int iconPlayPause;
        String text;
        if(isPaused) {
            iconPlayPause = R.drawable.ic_play_arrow;
            text = "Play";
        } else {
            iconPlayPause = R.drawable.ic_action_playback_pause;
            text = "Pause";
        }

        String albumArt = (songs.get(playingTrack)).getAlbumArt();
        Bitmap albumIcon;
        albumIcon = Bitmap.createScaledBitmap(icon, 128, 128, false);
        if (albumArt != null) {
            try {
                albumIcon = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(albumArt));
            }
            catch(Exception e){
                Log.e("ICON ALBUMART NOTIFIC", "Error setting Bitmap from URI");
            }
        }

        Notification notification =
                new NotificationCompat.Builder(this)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setTicker("Reproduciendo ")
                        .setContentIntent(pendingIntent)
                        // Agrego los botones para controlar la reproduccion
                        .addAction(R.drawable.ic_action_playback_prev, "Previous", prevPendingIntent) // #1
                        .addAction(iconPlayPause, text, pausePendingIntent)  // #2
                        .addAction(R.drawable.ic_action_playback_next, "Next", nextPendingIntent)     // #3
                        .addAction(R.drawable.ic_action_playback_next, "Stop", nextPendingIntent)     // #3
                        .setOngoing(true)
                        .setContentTitle("Final Music Player")
                        .setContentText(songs.get(playingTrack).getSongName())
                        //.setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false)) // Se puede poner la imagen del album
                        .setLargeIcon(albumIcon)
                        .build();

        startForeground(1, notification);
    }

    private Long getPlayListID(String playlistName){
        Long result = -1L;
        String[] projection1 = {
                MediaStore.Audio.Playlists._ID,
                MediaStore.Audio.Playlists.NAME
        };
        Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                projection1,
                MediaStore.Audio.Playlists.NAME+ " =? ",
                new String[] {playlistName},
                null);
        cursor.moveToFirst();

        Long playlist_id = cursor.getLong(0);

        if(cursor != null){
            cursor.moveToFirst();
            result = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Playlists._ID));
        }
        cursor.close();
        return result;
    }

    private class SongLoader extends AsyncTask<Object,Object,Object> {
        private boolean queue = false;
        @Override
        protected Object doInBackground(Object... params) {
            this.queue = (boolean)params[4];
            //Cargando el cursor
            Log.d("DoInBack","Creating Cursor");
            Cursor data = MusicService.this.getContentResolver().query(
                    (Uri)params[0],
                    (String[])params[1],
                    (String)params[2],
                    (String[])params[3],
                    null);
            // /Recorriendo el cursor
            Log.d(TAG, "Loading Songs");
            if (data != null) {
                while(data.moveToNext()){
                    String a = data.getString(data.getColumnIndex(MediaStore.Audio.Media.DURATION));
                    if(a == null) // un archivo ocasionaba problemas porque tenia duracion 0
                        a = Integer.toString(0);

                    Song s = new Song(  data.getString(data.getColumnIndex(Codes.TAG_SONG_TITLE)),
                                        artistName,
                                        data.getString(data.getColumnIndex(MediaStore.Audio.Media.ALBUM)),
                                        //Long.parseLong(data.getString(data.getColumnIndex(MediaStore.Audio.Media.DURATION))),//duration,MediaStore.Audio.Media.DURATION,
                                        Long.parseLong(a),//duration,MediaStore.Audio.Media.DURATION,
                                        albumArt,
                                        data.getString(data.getColumnIndex(MediaStore.Audio.Media.DATA)));
                    Log.i("DoInBack", "Song for load: " + s.getSongName());
                    songs.add(s);
                }
                Log.d(TAG,"Closing Cursor");
                data.close();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Object o) {
            Log.d("Lifecycle", "SERVICE: onPostExecute");
            Log.i(TAG,"Songs Loaded");

            for(Song s: songs){
                Log.i(TAG, s.toString());
            }
            Log.i(TAG, "Songs size" + Integer.toString(songs.size()));
            if( !this.queue || songs.size() == 1 ) { //quiere decir que solo esta la cancion anadida
                MusicService.this.play();
            }
            sendResult();
        }
    }

    private class SongLoaderFromPlaylist extends AsyncTask<Object,Object,Object> {

        @Override
        protected Object doInBackground(Object... params) {
            //Cargando el cursor
            Log.d(TAG,"Creating Cursor");
            Cursor cursor = getContentResolver().query(
                    (Uri)params[0],
                    (String[])params[1],
                    (String)params[2],
                    (String[])params[3],
                    null);

            // /Recorriendo el cursor
            Log.d(TAG, "Loading Songs");
            if (cursor != null) {
                while(cursor.moveToNext()){
                    String a = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.DURATION));
                    artistName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.ARTIST));
                    if(a == null) // un archivo ocasionaba problemas porque tenia duracion 0
                        a = Integer.toString(0);

                    // Acá debo instanciar albumArt porque en una ejecución nueva viene con Null, probablemente necesite guardar
                    // la Uri del albumArt, cuando guardo la playList, para poder instanciarla aquí y que se respete el albumArt de
                    // cada cancion de la PlayList.

                    Song s = new Song(  cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.TITLE)),
                            artistName,
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.ALBUM)),
                            //Long.parseLong(data.getString(data.getColumnIndex(MediaStore.Audio.Media.DURATION))),//duration,MediaStore.Audio.Media.DURATION,
                            Long.parseLong(a),//duration,MediaStore.Audio.Media.DURATION,
                            albumArt,
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.DATA)));

                    songs.add(s);
                }
                Log.d(TAG,"Closing Cursor");
                cursor.close();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            Log.d("Lifecycle", "SERVICE: onPostExecute");
            Log.d(TAG,"Songs Loaded from PlayList");

            for(Song s: songs){
                Log.d(TAG, s.toString());
            }
            Log.d(TAG, Integer.toString(songs.size()));
            MusicService.this.play();
            sendResult();
        }
    }

    public class MusicServiceBinder extends Binder implements ICallService {

        public MusicService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MusicService.this;
        }

        @Override
        public void play() throws IOException {
            MusicService.this.play();
        }

        @Override
        public void stop() {
            MusicService.this.stop();
        }

        @Override
        public void pause() {
            MusicService.this.pause();
        }

        @Override
        public void nextSong() {
            MusicService.this.nextSong();
        }

        @Override
        public void previousSong() {
            MusicService.this.previousSong();
        }

        @Override
        public void resume() {
            MusicService.this.resume();
        }

        @Override
        public int getCurrentPosition() {
            return MusicService.this.getCurrentPosition();
        }

        @Override
        public void seekTo(int position) {
            MusicService.this.seekTo(position);
        }

        @Override
        public void playSelectedSong(int position) {
            MusicService.this.playSelectedSong(position);
        }

        @Override
        public void getResult() {
            MusicService.this.sendResult();
        }

        @Override
        public boolean isPlaying() {
           return MusicService.this.isPlaying();
        }

        @Override
        public boolean isPaused() {
            return MusicService.this.isPaused();
        }

    }
}
