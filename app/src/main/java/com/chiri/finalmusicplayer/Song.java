package com.chiri.finalmusicplayer;

/**
 * Created by chiri on 15/10/16.
 */

public class Song {

    private String songName;
    private String artistName;
    private String albumName;
    private Long duration;
    private String uri; //URI de la cancion en el dispositivo

    public Song(String songName, String artistName, String albumName, Long duration, String uri) {
        this.songName = songName;
        this.artistName = artistName;
        this.albumName = albumName;
        this.duration = duration;
        this.uri = uri;
    }

    public String getSongName() {
        return songName;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return "Song{" +
                "songName='" + songName + '\'' +
                ", artistName='" + artistName + '\'' +
                ", albumName='" + albumName + '\'' +
                ", duration=" + duration +
                ", uri='" + uri + '\'' +
                '}';
    }
}
