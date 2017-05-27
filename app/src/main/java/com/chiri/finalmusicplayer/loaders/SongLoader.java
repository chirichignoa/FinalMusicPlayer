package com.chiri.finalmusicplayer.loaders;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.chiri.finalmusicplayer.activities.LibraryActivity;

/**
 * Created by chiri on 21/04/17.
 */

public class SongLoader implements LoaderManager.LoaderCallbacks<Cursor> {
    private Context context;

    public SongLoader(Context context){
        this.context = context;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this.context,
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{   "_id",
                                MediaStore.Audio.Media.TITLE,
                                MediaStore.Audio.Media.ARTIST,
                                MediaStore.Audio.Media.DURATION,
                                MediaStore.Audio.Media.ALBUM,
                                MediaStore.Audio.Media.DATA}, //Proyeccion: son las columnas que retornara la query
                //selection.toString(), //Seleccion:la fila que devolvera la query, la que coincida con el album
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d("SongLoader", "Carga finalizada");
        ((LibraryActivity) context).getSongAdapter().swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d("SongLoader", "Loader reset");
        ((LibraryActivity) context).getSongAdapter().swapCursor(null);
    }
}
