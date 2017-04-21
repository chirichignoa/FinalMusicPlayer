package com.chiri.finalmusicplayer;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

/**
 * Created by chiri on 15/10/16.
 */

public class AlbumLoader implements LoaderManager.LoaderCallbacks<Cursor> {
    private Context context;

    public AlbumLoader(Context context){
        Log.d("AlbumLoader", "Creando loader");
        this.context = context;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.d("AlbumLoader", "Creando cursor");
        return new CursorLoader(this.context,
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, //Uri Todo:ver si anda con memoria interna
                new String[]{"_id",MediaStore.Audio.AlbumColumns.ALBUM,
                        MediaStore.Audio.AlbumColumns.ARTIST,
                        MediaStore.Audio.AlbumColumns.ALBUM_ART}, //Proyeccion: son las columnas que retornara la query
                //selection.toString(), //Seleccion:la fila que devolvera la query, la que coincida con el album
                null,
                //new String[]{((MainActivity) context).getArtist()}, //
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d("AlbumLoader", "Carga finalizada");
        ((LibraryActivity) context).getAdapter().swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d("AlbumLoader", "Loader reset");
        ((LibraryActivity) context).getAdapter().swapCursor(null);
    }
}