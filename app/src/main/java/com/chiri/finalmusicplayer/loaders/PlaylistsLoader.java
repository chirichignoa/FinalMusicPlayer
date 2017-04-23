package com.chiri.finalmusicplayer.loaders;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.chiri.finalmusicplayer.LibraryActivity;

/**
 * Created by chiri on 21/04/17.
 */

public class PlaylistsLoader implements LoaderManager.LoaderCallbacks<Cursor> {
    private Context context;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        ((LibraryActivity) context).getPlaylistsAdapter().swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ((LibraryActivity) context).getPlaylistsAdapter().swapCursor(null);
    }
}
