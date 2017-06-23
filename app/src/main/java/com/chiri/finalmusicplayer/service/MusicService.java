package com.chiri.finalmusicplayer.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
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
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.chiri.finalmusicplayer.R;
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
    private static List<Song> songs = new ArrayList<>();
    private static int playingTrack = 0;
    private static boolean isPlaying = false, isPaused = false;
    private LocalBroadcastManager broadcaster;

    public MusicService() {

    }

    @Override
    public void onCreate() {
        broadcaster = LocalBroadcastManager.getInstance(this);
        initMediaPlayer();
    }

//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        this.decodeIntent(intent);
//
//        return START_NOT_STICKY;
//    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if(isPlaying){
            sendResult();
        } else {
            decodeIntent(intent);
        }
        return null;
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.reset();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setVolume(75, 75);
    }

    private void decodeIntent(Intent intent){
        final Bundle bundle = intent.getExtras();
        SongLoader sl = new SongLoader();
        switch (bundle.getString(Codes.TAG_TYPE,"NULL")){
            case Codes.TAG_SONG: {
                MusicService.this.stop();
                songs.clear();
                playingTrack = 0;

                songName = bundle.getString(Codes.TAG_SONG_TITLE);
                artistName = bundle.getString(Codes.TAG_ARTIST);
                albumArt = bundle.getString(Codes.TAG_ALBUMART);
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] projection = { MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.DATA};
                String selection = "IS_MUSIC != 0 AND " + MediaStore.Audio.Media.TITLE + "=? AND "
                        + MediaStore.Audio.Media.ARTIST + "=?";
                String[] selectionArgs = {songName, artistName};
                sl.execute(uri,projection,selection,selectionArgs);
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
                sl.execute(uri,projection,selection,selectionArgs);
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
        sendResult();
        mediaPlayer.start();
        isPlaying = true;
        isPaused = false;
    }

    public void sendResult() {
        Intent intent = new Intent(Codes.TAG_SEND_RESULT);
        intent.putExtra(Codes.TAG_SONG, songs.get(playingTrack));
        broadcaster.sendBroadcast(intent);
    }

    public void stop() {
        Log.i("Player Info","Paro");
        isPlaying = false;
        playingTrack = 0;
        mediaPlayer.stop();
        mediaPlayer.reset();
        //stop foreground
    }

    public void pause() {
        Log.i("Player Info","Esta en pausa");
        isPaused = true;
        mediaPlayer.pause();
    }

    public void resume(){
        Log.i("Player Info","Resumiendo");
        this.mediaPlayer.start();
    }

    public void nextSong() {
        Log.i("Player Info","Cancion siguiente");
        if(playingTrack+1 < songs.size()){
            playingTrack += 1;
            mediaPlayer.reset();
            this.play();
        }else{
            this.stop();
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
        this.nextSong();
    }

    private void addToQueue(){

    }

    private void startForeground(){
        Intent notificationIntent = new Intent(this, MusicService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_launcher);

        Intent previousIntent = new Intent(this, MusicService.class);
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, 0);

        Intent playIntent = new Intent(this, MusicService.class);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);

        Intent nextIntent = new Intent(this, MusicService.class);
        PendingIntent pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Music Player")
                .setTicker("Reproduciendo ")
                .setContentText(songs.get(playingTrack).getSongName())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_media_previous, "Previous",
                        ppreviousIntent)
                .addAction(android.R.drawable.ic_media_play, "Play",
                        pplayIntent)
                .addAction(android.R.drawable.ic_media_next, "Next",
                        pnextIntent).build();
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

        @Override
        protected Object doInBackground(Object... params) {
            //Cargando el cursor
            Log.d(TAG,"Creating Cursor");
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

                    Log.d("ALBUM-ART", albumArt);

                    songs.add(s);
                }
            }

            Log.d(TAG,"Closing Cursor");
            data.close();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            Log.d(TAG,"Songs Loaded");
            for(Song s: songs){
                Log.d(TAG, s.toString());
            }
            Log.d(TAG, Integer.toString(songs.size()));
            MusicService.this.play();
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
            Log.d(TAG,"Songs Loaded");
            for(Song s: songs){
                Log.d(TAG, s.toString());
            }
            Log.d(TAG, Integer.toString(songs.size()));
            MusicService.this.play();
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
        public void addQueue() {
            MusicService.this.addToQueue();
        }
    }
}
