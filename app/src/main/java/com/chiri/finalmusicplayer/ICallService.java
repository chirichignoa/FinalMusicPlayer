package com.chiri.finalmusicplayer;

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
}
