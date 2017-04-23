package com.chiri.finalmusicplayer.loaders;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.chiri.finalmusicplayer.LibraryActivity;

/**
 * Created by chiri on 21/04/17.
 */

public class PlaylistsLoader implements LoaderManager.LoaderCallbacks<Cursor> {
    private Context context;

    public PlaylistsLoader(Context context){
        Log.d("PlayListLoader", "Creando loader");
        this.context = context;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(this.context,
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                new String[]{ MediaStore.Audio.Playlists._ID,
                        MediaStore.Audio.Playlists.NAME}, //Proyeccion: son las columnas que retornara la query
                //selection.toString(), //Seleccion:la fila que devolvera la query, la que coincida con el album
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d("PlaylistLoader", "Carga finalizada");
        ((LibraryActivity) context).getPlaylistsAdapter().swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d("PlaylistLoader", "Loader reset");
        ((LibraryActivity) context).getPlaylistsAdapter().swapCursor(null);
    }
}
