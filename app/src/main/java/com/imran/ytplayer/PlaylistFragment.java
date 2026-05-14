package com.imran.ytplayer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlaylistFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private PlaylistAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_list, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyView = view.findViewById(R.id.empty_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            adapter = activity.getPlaylistAdapter();
        }

        if (adapter == null) {
            adapter = new PlaylistAdapter(null);
        }
        recyclerView.setAdapter(adapter);

        loadData();
        return view;
    }

    private void loadData() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;

        List<PlaylistItem> playlists = activity.getPlaylists();
        if (playlists != null && !playlists.isEmpty()) {
            adapter.setVideos(playlists);
            emptyView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText("Sign in to see your playlists");
        }
    }
}
