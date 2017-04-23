package com.chiri.finalmusicplayer.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by chiri on 20/10/16.
 */

public class Album implements Parcelable{
    private String albumName;
    private String artistName;
    private String albumArtUri;
    private String albumID;

    public static final Creator<Album> CREATOR = new Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };

    public Album(String albumName, String artistName, String albumArtUri, String albumID){
        this.albumName = albumName;
        this.artistName = artistName;
        this.albumArtUri = albumArtUri;
        this.albumID = albumID;
    }

    protected Album(Parcel in) {
        albumName = in.readString();
        artistName = in.readString();
        albumArtUri = in.readString();
        albumID = in.readString();
    }


    public String getAlbumName() {
        return albumName;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getAlbumArtUri() {
        return albumArtUri;
    }

    public String getAlbumID(){return albumID;}

    @Override
    public String toString() {
        return "Album{" +
                "albumName='" + albumName + '\'' +
                ", artistName='" + artistName + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(albumName);
        parcel.writeString(artistName);
        parcel.writeString(albumArtUri);
        parcel.writeString(albumID);
    }

}
