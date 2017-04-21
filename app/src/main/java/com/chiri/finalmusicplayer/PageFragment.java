package com.chiri.finalmusicplayer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

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


        adapter = new AlbumAdapter(getContext(),null,false);
        Log.d("PageFragment", "Seteando adapter a Library");
        LibraryActivity.setAdapter(adapter);
        Log.d("PageFragment", "Seteando adapter");
        listView.setAdapter(adapter);
        getLoaderManager().initLoader(0, null, new AlbumLoader(getContext())); //id,args,callback
        return view;
    }

}