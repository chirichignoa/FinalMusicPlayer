package com.chiri.finalmusicplayer.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.chiri.finalmusicplayer.activities.LibraryActivity;
import com.chiri.finalmusicplayer.R;
import com.chiri.finalmusicplayer.adapters.AlbumAdapter;
import com.chiri.finalmusicplayer.adapters.PlaylistsAdapter;
import com.chiri.finalmusicplayer.adapters.SongAdapter;
import com.chiri.finalmusicplayer.loaders.AlbumLoader;
import com.chiri.finalmusicplayer.loaders.PlaylistsLoader;
import com.chiri.finalmusicplayer.loaders.SongLoader;
import com.chiri.finalmusicplayer.model.Codes;
import com.chiri.finalmusicplayer.service.MusicService;

import static android.app.Activity.RESULT_OK;


/**
 * Created by chiri on 20/04/17.
 */

public class PageFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";
    public static final String ARG_TYPE = "ARG_TYPE";

    private int mPage;
    private String typeTab;

    private ListView listView;
    private CursorAdapter adapter;
    private ImageButton overflowButton;
    private MusicService musicService;
    private BroadcastReceiver receiver;


    public static PageFragment newInstance(int page, String typeTab) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        args.putString(ARG_TYPE, typeTab);
        PageFragment fragment = new PageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);
        this.typeTab = getArguments().getString(ARG_TYPE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_page, container, false);
        listView = (ListView)view.findViewById(R.id.listView);
        switch(mPage){
            case 0:
                SongAdapter songAdapter = new SongAdapter(getContext(),null,false);
                ((LibraryActivity)getActivity()).setSongAdapter(songAdapter);
                listView.setAdapter(songAdapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        String songName = ((TextView)view.findViewById(R.id.mainTitle)).getText().toString();
                        String artistName = ((TextView)view.findViewById(R.id.subTitle)).getText().toString();

                        String albumName = getAlbumName(getContext(), songName);
                        String albumArt = getAlbumArt(getContext(), albumName);

                        Intent intent = new Intent();
                        intent.putExtra(Codes.TAG_TYPE, Codes.TAG_SONG);
                        intent.putExtra(Codes.TAG_SONG_TITLE, songName);
                        intent.putExtra(Codes.TAG_ARTIST, artistName);
                        intent.putExtra(Codes.TAG_ALBUMART, albumArt);
                        getActivity().setResult(RESULT_OK, intent);
                        getActivity().finish();
                    }
                });
                getLoaderManager().initLoader(0, null, new SongLoader(getContext())); //id,args,callback}

                break;
            case 1:
                AlbumAdapter albumAdapter = new AlbumAdapter(getContext(),null,false);
                ((LibraryActivity)getActivity()).setAlbumAdapter(albumAdapter);
                listView.setAdapter(albumAdapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String albumName = ((TextView)view.findViewById(R.id.mainTitle)).getText().toString();
                        String artistName = ((TextView)view.findViewById(R.id.subTitle)).getText().toString();
                        String albumArt = getAlbumArt(getContext(), albumName);

                        Intent intent = new Intent(getActivity(),MusicService.class);
                        intent.putExtra(Codes.TAG_TYPE, Codes.TAG_ALBUM);
                        intent.putExtra(Codes.TAG_ALBUM_TITLE, albumName);
                        intent.putExtra(Codes.TAG_ALBUM_ARTIST, artistName);
                        intent.putExtra(Codes.TAG_ALBUMART, albumArt);
                        getActivity().setResult(RESULT_OK, intent);
                        getActivity().finish();
                    }
                });
                getLoaderManager().initLoader(0, null, new AlbumLoader(getContext())); //id,args,callback}
                break;
            case 2:
                PlaylistsAdapter playlistsAdapter = new PlaylistsAdapter(getContext(),null,false);
                Log.d("PageFragment", "Seteando AlbumAdapter a Library");
                ((LibraryActivity)getActivity()).setPlaylistsAdapter(playlistsAdapter);
                Log.d("PageFragment", "Seteando AlbumAdapter");
                listView.setAdapter(playlistsAdapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String playlistName = ((TextView)view.findViewById(R.id.mainTitle)).getText().toString();

                        Intent intent = new Intent(getActivity(),MusicService.class);
                        intent.putExtra(Codes.TAG_TYPE, Codes.TAG_PLAYLIST);
                        intent.putExtra(Codes.TAG_PLAYLIST_NAME, playlistName);
                        getActivity().setResult(RESULT_OK, intent);
                        getActivity().finish();
                    }
                });
                getLoaderManager().initLoader(0, null, new PlaylistsLoader(getContext())); //id,args,callback}
                break;
        }
        return view;
    }

    private String getAlbumName(Context c, String selection){
        Cursor cursor = c.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] {
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ALBUM
                },
                MediaStore.Audio.Media.TITLE + "=?",
                new String[] {selection},
                null);
        String res = null;
        if(cursor.moveToFirst()){
            res = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
        }
        cursor.close();
        return res;
    }

    private String getAlbumArt(Context c, String selection){ //from albumName
        Cursor cursor = c.getContentResolver().query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[] {
                        MediaStore.Audio.Albums._ID,
                        MediaStore.Audio.AlbumColumns.ALBUM,
                        MediaStore.Audio.AlbumColumns.ALBUM_ART},
                MediaStore.Audio.AlbumColumns.ALBUM + "=?",
                new String[] {selection},
                null);

        String path = null;
        String albumID = null;
        if (cursor.moveToFirst()) {
            path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
        }
        if(path == null){
            path = "android.resource://com.chiri.finalmusicplayer/drawable/no_art";
        }
        cursor.close();
        return path;
    }
}