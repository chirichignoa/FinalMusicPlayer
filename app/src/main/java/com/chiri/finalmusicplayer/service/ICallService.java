package com.chiri.finalmusicplayer.service;

import java.io.IOException;

/**
 * Created by chiri on 24/10/16.
 */

public interface ICallService {

    void play() throws IOException;
    void stop();
    void pause();
    void nextSong();
    void previousSong();
    void resume();
    void addQueue();
    int getCurrentPosition();
    void seekTo(int position);
    void playSelectedSong(int position);
}
