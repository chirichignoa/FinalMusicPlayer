package com.chiri.finalmusicplayer.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.chiri.finalmusicplayer.R;

import java.util.List;

/**
 * Created by chiri on 21/04/17.
 */

public class SongAdapter extends CursorAdapter  {

    private LayoutInflater inflater; //Sirve para inyectar cada vistas al padre(y con las caracteristicas del padre)

    /**autoRequery no se recomienda en true,
     If true the adapter will call requery() on the cursor
     whenever it changes, so the most recent data is always displayed. **/
    public SongAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View v = this.inflater.inflate(R.layout.list_item,parent,false);
        return v;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        String song = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)); //nombre de la cancion
        Log.d("SONG", song);
        final TextView songTitle = (TextView) view.findViewById(R.id.mainTitle);
        songTitle.setText(song);

        String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)); //nombre del artista
        TextView artistName = (TextView)view.findViewById(R.id.subTitle);
        artistName.setText(artist);

        ImageButton overflowButton = (ImageButton) view.findViewById(R.id.overflowButton);
        overflowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(context, v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.actions, popup.getMenu());
                popup.show();
            }
        });

        String albumName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)); //nombre del album
        Cursor cursorArt = context.getContentResolver().query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[] {
                        MediaStore.Audio.Albums._ID,
                        MediaStore.Audio.AlbumColumns.ALBUM,
                        MediaStore.Audio.AlbumColumns.ALBUM_ART},
                MediaStore.Audio.AlbumColumns.ALBUM+ "=?",
                new String[] {albumName},
                null);

        String albumArt = null;
        if (cursorArt.moveToFirst()) {
            albumArt = cursorArt.getString(cursorArt.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
        }
        cursorArt.close();

        ImageView image = (ImageView) view.findViewById(R.id.image);
        if(albumArt != null) {
            image.setImageURI(Uri.parse(albumArt));
        }
        else{
            image.setImageResource(R.drawable.no_art);
        }
    }
}
