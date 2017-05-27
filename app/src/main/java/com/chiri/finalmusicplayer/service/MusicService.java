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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chiri on 26/05/17.
 */

public class MusicService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    public final static String EXTRA_SONG = "Song Name";
    public final static String TAG = MusicService.class.getCanonicalName();

    private static MediaPlayer mediaPlayer;
    private static String artistName, songName, albumArt;
    private static List<Song> songs = new ArrayList<>();
    private static int playingTrack = 0;
    private static boolean isPlaying = false, isPaused = false;
    private LocalBroadcastManager broadcaster;

    public MusicService() {

    }

    @Override
    public void onCreate() {
        broadcaster = LocalBroadcastManager.getInstance(this);
        //initMediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SongLoader sl = new SongLoader();
        this.decodeIntent(intent, sl);
        //this.registerListeners();

//        sl.execute(Uri uri, String[] projection, String selection, String[] selectionArgs);
        return START_NOT_STICKY;
    }

    private void decodeIntent(Intent intent, SongLoader sl){
        final Bundle bundle = intent.getExtras();
        switch (bundle.getString(Codes.TAG_TYPE,"NULL")){
            case Codes.TAG_SONG: {
                //clear lista actual
                MusicService.this.stop();
                songs.clear();
                playingTrack = 0;

                songName = bundle.getString(Codes.TAG_TITLE);
                artistName = bundle.getString(Codes.TAG_ARTIST);
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] projection = { MediaStore.Audio.Media.TITLE,
                                        MediaStore.Audio.Media.ARTIST,
                                        MediaStore.Audio.Media.DURATION,
                                        MediaStore.Audio.Media.ALBUM,
                                        MediaStore.Audio.Media.DATA};
                String selection = MediaStore.Audio.Media.TITLE + "=? AND " + MediaStore.Audio.Media.ARTIST + "=?";
                String[] selectionArgs = {songName, artistName};
                sl.execute(uri,projection,selection,selectionArgs);
            }
            break;
            case Codes.TAG_ALBUM: {
                //clear lista actual
                MusicService.this.stop();
                songs.clear();
                playingTrack = 0;
            }
            break;
            case Codes.TAG_PLAYLIST: {
                //clear lista actual
                MusicService.this.stop();
                songs.clear();
                playingTrack = 0;
            }
            default:
                break;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (isPlaying) {
           this.stop(); //posible llamada al clear
        }
        initMediaPlayer();
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

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

    }

    private void play() {

    }

    private void stop() {

    }

    private void pause(){

    }

    private void resume(){

    }

    private void previousSong() {

    }

    private void nextSong() {

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
            while(data.moveToNext()){
                String a = data.getString(data.getColumnIndex(MediaStore.Audio.Media.DURATION));
                if(a == null) // un archivo ocasionaba problemas porque tenia duracion 0
                    a = Integer.toString(0);
                Song s = new Song(  songName,
                                    artistName,
                                    data.getString(data.getColumnIndex(MediaStore.Audio.Media.ALBUM)),
                                    //Long.parseLong(data.getString(data.getColumnIndex(MediaStore.Audio.Media.DURATION))),//duration,MediaStore.Audio.Media.DURATION,
                                    Long.parseLong(a),//duration,MediaStore.Audio.Media.DURATION,
                                    data.getString(data.getColumnIndex(MediaStore.Audio.Media.DATA)));

                MusicService.this.songs.add(s);
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
        }
    }
}
