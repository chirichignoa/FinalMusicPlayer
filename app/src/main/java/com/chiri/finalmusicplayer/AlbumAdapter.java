package com.chiri.finalmusicplayer;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by chiri on 15/10/16.
 */

public class AlbumAdapter extends CursorAdapter {
    LayoutInflater inflater; //Sirve para inyectar cada vistas al padre(y con las caracteristicas del padre)

    /**autoRequery no se recomienda en true,
     If true the adapter will call requery() on the cursor
     whenever it changes, so the most recent data is always displayed. **/
    public AlbumAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) { //crea la vista (list_item)
        View v = this.inflater.inflate(R.layout.list_item,viewGroup,false);
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) { //llena la vista (list_item)
        String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM)); //nombre del album
        TextView albumTittle = (TextView) view.findViewById(R.id.albumTittle);
        albumTittle.setText(album);

        String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ARTIST)); //nombre del artista
        TextView artistName = (TextView)view.findViewById(R.id.artistName);
        artistName.setText(artist);

        String albumArt = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM_ART)); //art del album
        //System.out.println(album+" "+albumArt);
        ImageView image = (ImageView) view.findViewById(R.id.albumArt);
        if(albumArt != null) {
            image.setImageURI(Uri.parse(albumArt));
        }
        else{
           image.setImageResource(R.drawable.no_art);
        }
    }
}
