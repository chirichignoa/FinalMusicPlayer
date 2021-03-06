package com.chiri.finalmusicplayer.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by chiri on 15/10/16.
 */

public class Song implements Parcelable {

    private String songName;
    private String artistName;
    private String albumName;
    private long duration;
    private String albumArt;


    private String uri; //URI de la cancion en el dispositivo

    public Song(String songName, String artistName, String albumName, long duration, String albumArt, String uri) {
        this.songName = songName;
        this.artistName = artistName;
        this.albumName = albumName;
        this.duration = duration;
        this.albumArt = albumArt;
        this.uri = uri;
    }

    public Song(Parcel in) {
        songName = in.readString();
        artistName = in.readString();
        albumName = in.readString();
        duration = in.readLong();
        albumArt = in.readString();
        uri = in.readString();
    }


    public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>() {

        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public String getSongName() {
        return songName;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getAlbumArt() {
        return albumArt;
    }

    public Long getDuration(){
        return duration;
    }

    public String getUri() {
        return uri;
    }

    public void setAlbumArt(String albumArt) {
        this.albumArt = albumArt;
    }

    @Override
    public String toString() {
        return "Song{" +
                "songName='" + songName + '\'' +
                ", artistName='" + artistName + '\'' +
                ", albumName='" + albumName + '\'' +
                ", duration=" + duration +
                ", uri='" + uri + '\'' +
                ", albumArt='" + albumArt + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(songName);
        dest.writeString(artistName);
        dest.writeString(albumName);
        dest.writeLong(duration);
        dest.writeString(albumArt);
        dest.writeString(uri);
    }
}
