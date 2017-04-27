package com.chiri.finalmusicplayer.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chiri.finalmusicplayer.R;

/**
 * Created by chiri on 21/04/17.
 */

public class PlaylistsAdapter extends CursorAdapter {
    LayoutInflater inflater; //Sirve para inyectar cada vistas al padre(y con las caracteristicas del padre)

    public PlaylistsAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        Log.d("Playlistdapter", "Creando la vista");
        View v = this.inflater.inflate(R.layout.list_item,parent,false);
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String playlist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME)); //nombre de la playlist
        Log.d("Playlist", playlist);
        //VER SI NO HAY PLAYLISTS PONER MENSAJE

        TextView songTitle = (TextView) view.findViewById(R.id.mainTitle);
        songTitle.setText(playlist);

        TextView artistName = (TextView)view.findViewById(R.id.subTitle);
        artistName.setVisibility(View.INVISIBLE);

        ImageView image = (ImageView) view.findViewById(R.id.image);
        image.setImageResource(R.drawable.music_note);
    }
}
