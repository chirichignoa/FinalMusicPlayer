package com.chiri.finalmusicplayer;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

/**
 * Created by chiri on 15/10/16.
 */

public class AlbumLoader implements LoaderManager.LoaderCallbacks<Cursor> {
    private Context context;

    public AlbumLoader(Context context){
        this.context = context;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
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
//        ((LibraryActivity) context).getAdapter().swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
//        ((LibraryActivity) context).getAdapter().swapCursor(null);
    }
}