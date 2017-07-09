package com.chiri.finalmusicplayer.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.chiri.finalmusicplayer.R;
import com.chiri.finalmusicplayer.model.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by negro on 09/06/17.
 */

public class CurrentPlayListAdapter extends BaseAdapter {

    static class ViewHolder {
        TextView title;
        TextView subTitle;
        ImageView image;
        ImageButton overflow;
    }

    private List<Song> songs = new ArrayList<>();
    private Context c = null;


    public CurrentPlayListAdapter(Context c, List<Song> songs){
        this.songs = songs;
        this.c = c;
    }

    @Override
    public int getCount() {
        return this.songs.size();
    }

    @Override
    public Object getItem(int position) {
        return this.songs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount()
    {
        return 1;
    }

    @Override
    public int getItemViewType(int position)
    {
        return 0;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final TextView title, subTitle;
        final ImageView image;
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item,parent,false);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.mainTitle);
            holder.subTitle = (TextView) convertView.findViewById(R.id.subTitle);
            holder.image = (ImageView) convertView.findViewById(R.id.image);
            holder.overflow = (ImageButton) convertView.findViewById(R.id.overflowButton);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Song s = this.songs.get(position);

        holder.title.setTextColor(Color.BLACK);
        holder.title.setText(s.getSongName());
        holder.subTitle.setTextColor(Color.BLACK);
        holder.subTitle.setText(s.getArtistName());
        holder.image.setImageURI(Uri.parse(s.getAlbumArt()));
        holder.overflow.setVisibility(View.INVISIBLE);
        return convertView;
    }



    public void add(Song s){
        this.songs.add(s);
        super.notifyDataSetChanged();
    }
}
