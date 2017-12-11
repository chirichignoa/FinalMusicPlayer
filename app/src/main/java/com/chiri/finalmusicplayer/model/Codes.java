package com.chiri.finalmusicplayer.model;

import android.provider.MediaStore;

/**
 * Created by chiri on 26/05/17.
 */

public final class Codes {
    public final static String TAG_TYPE = "TYPE";
    public final static String TAG_SONG = "SONG";
    public final static String TAG_ALBUM = "ALBUM";
    public final static String TAG_PLAYLIST = "PLAYLIST";
    public final static String TAG_ACTION = "ACTION";


    public final static String TAG_SONG_TITLE = MediaStore.Audio.Media.TITLE;
    public final static String TAG_ALBUM_TITLE = MediaStore.Audio.AlbumColumns.ALBUM;
    public final static String TAG_ALBUM_ARTIST = MediaStore.Audio.AlbumColumns.ARTIST;
    public final static String TAG_ARTIST = MediaStore.Audio.Media.ARTIST;
    public final static String TAG_ALBUMART = MediaStore.Audio.AlbumColumns.ALBUM_ART;
    public final static String TAG_PLAYLIST_NAME = MediaStore.Audio.Playlists.NAME;

    public final static String TAG_ADD_SONG_QUEUE = "ADD_SONG_QUEUE";
    public final static String TAG_ADD_ALBUM_QUEUE = "ADD_ALBUM_QUEUE";
    public final static String TAG_SEND_RESULT = "SEND_RESULT";
    public final static String TAG_SEND_CURRENT_PLAYLIST = "SEND_CURRENT_PLAYLIST";
    public final static String TAG_CURRENT_PLAYLIST = "CURRENT_PLAYLIST";


    public final static String ACTION_FOREGROUND = "FOREGROUND";
    public final static String ACTION_PREVIOUS = "PREVIOUS";
    public final static String ACTION_PLAYPAUSE = "PLAYPAUSE";
    public final static String ACTION_NEXT = "NEXT";




}
