package com.chiri.finalmusicplayer;

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
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.chiri.finalmusicplayer.Song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chiri on 24/10/16.
 */

public class MusicService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener{

    public final static String EXTRA_SONG = "Song Name";

    private static MediaPlayer mediaPlayer;
    private static String albumID;
    private static List<Song> songs = new ArrayList<>();
    private static int playingTrack = 0;
    private static boolean isPlaying = false, isPaused = false;
    private LocalBroadcastManager broadcaster;


    public MusicService() {
    }

    @Override
    public void onCreate() {
        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        if(isPlaying){
            this.stop();
        }
        initMediaPlayer();
        Bundle extras = arg0.getExtras();
        albumID = extras.getString("AlbumID");
        return new MusicServiceBinder();
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.reset();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setVolume(75,75);
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

    public void play() {
        try{
            this.songs = getSongs();
            Log.i("Songs", Integer.toString(songs.size()));
            for(Song s: songs){
                System.out.println(s.toString());
            }
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
       sendResult();
       Log.i("Player Info","Empieza a reproducir");
       mediaPlayer.start();
       isPlaying = true;
       isPaused = false;
    }

    public void sendResult() {
        Intent intent = new Intent(this.EXTRA_SONG);
        intent.putExtra(this.EXTRA_SONG, songs.get(playingTrack).getSongName());
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

    public class MusicServiceBinder extends Binder implements ICallService {

        MusicService getService() {
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
    }

    public List<Song> getSongs() {
        List<Song> result = new ArrayList<>();
        String selection = "IS_MUSIC != 0 AND ALBUM_ID = " + albumID;

        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
        };
        //final String sortOrder = MediaStore.Audio.AudioColumns.TITLE + " COLLATE LOCALIZED ASC";

        Cursor cursor = null;
        try {
            Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            cursor = getContentResolver().query(uri, projection, selection, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) { //si ya nos pasamos del ultimo
                    Song song = new Song(cursor.getString(0), cursor.getString(1),
                            cursor.getString(5), cursor.getLong(4), cursor.getString(2));
                    result.add(song);
                    cursor.moveToNext();
                }
            } else
                Log.e("Player Error", "Canciones en null");
        } catch (Exception e) {
            Log.e("Player Error", e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }
}