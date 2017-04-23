package com.chiri.finalmusicplayer.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;

import com.chiri.finalmusicplayer.LibraryActivity;
import com.chiri.finalmusicplayer.R;
import com.chiri.finalmusicplayer.adapters.AlbumAdapter;
import com.chiri.finalmusicplayer.adapters.SongAdapter;
import com.chiri.finalmusicplayer.loaders.AlbumLoader;
import com.chiri.finalmusicplayer.loaders.SongLoader;


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
        View view = inflater.inflate(R.layout.fragment_page, container, false);

       //ACA va lo de llenar cada pestana

        listView = (ListView)view.findViewById(R.id.listView);

        switch(mPage){
            case 0:
                SongAdapter songAdapter = new SongAdapter(getContext(),null,false);
                ((LibraryActivity)getActivity()).setSongAdapter(songAdapter);
                listView.setAdapter(songAdapter);
                getLoaderManager().initLoader(0, null, new SongLoader(getContext())); //id,args,callback}
                break;
            case 1:
                AlbumAdapter albumAdapter = new AlbumAdapter(getContext(),null,false);
                ((LibraryActivity)getActivity()).setAlbumAdapter(albumAdapter);
                listView.setAdapter(albumAdapter);
                getLoaderManager().initLoader(0, null, new AlbumLoader(getContext())); //id,args,callback}
                break;
//            case 2:
//                PlaylistsAdapter playlistsAdapter = new PlaylistsAdapter(getContext(),null,false);
//                Log.d("PageFragment", "Seteando AlbumAdapter a Library");
//                ((LibraryActivity)getActivity()).setPlaylistsAdapter(playlistsAdapter);
//                Log.d("PageFragment", "Seteando AlbumAdapter");
//                listView.setAdapter(playlistsAdapter);
//                getLoaderManager().initLoader(0, null, new AlbumLoader(getContext())); //id,args,callback}
//                break;
        }
        return view;
    }

}