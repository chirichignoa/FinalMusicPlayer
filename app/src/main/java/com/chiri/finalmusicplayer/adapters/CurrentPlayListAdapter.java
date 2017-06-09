package com.chiri.finalmusicplayer.adapters;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import com.chiri.finalmusicplayer.R;

/**
 * Created by negro on 09/06/17.
 */

public class CurrentPlayListAdapter extends CursorAdapter {

    private LayoutInflater inflater; //Sirve para inyectar cada vistas al padre(y con las caracteristicas del padre)

    /**autoRequery no se recomienda en true,
     If true the adapter will call requery() on the cursor
     whenever it changes, so the most recent data is always displayed. **/
    public CurrentPlayListAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        Log.d("CurrentPlaylistAdapter", "Creando la vista");
        View v = this.inflater.inflate(R.layout.list_item,parent,false);
        return v;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        Log.d("CurrentPlayListBINDVIEW", "bindView");
    }
}
