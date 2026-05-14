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

public class VideoListFragment extends Fragment {

    private static final String ARG_TYPE = "type";
    public static final int TYPE_TRENDING = 0;
    public static final int TYPE_SUBSCRIPTIONS = 1;
    public static final int TYPE_LIBRARY = 3;

    private int type;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private VideoAdapter adapter;

    public static VideoListFragment newInstance(int type) {
        VideoListFragment fragment = new VideoListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getInt(ARG_TYPE);
        }
    }

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
            switch (type) {
                case TYPE_TRENDING:
                    adapter = activity.getTrendingAdapter();
                    break;
                case TYPE_SUBSCRIPTIONS:
                    adapter = activity.getSubscriptionAdapter();
                    break;
                case TYPE_LIBRARY:
                    adapter = activity.getLibraryAdapter();
                    break;
            }
        }

        if (adapter == null) {
            adapter = new VideoAdapter(null);
        }
        recyclerView.setAdapter(adapter);

        loadData();
        return view;
    }

    private void loadData() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;

        List<VideoItem> videos;
        switch (type) {
            case TYPE_TRENDING:
                videos = activity.getTrendingVideos();
                break;
            case TYPE_SUBSCRIPTIONS:
                videos = activity.getSubscriptionVideos();
                break;
            case TYPE_LIBRARY:
                videos = activity.getLibraryVideos();
                break;
            default:
                videos = activity.getTrendingVideos();
        }

        if (videos != null && !videos.isEmpty()) {
            adapter.setVideos(videos);
            emptyView.setVisibility(View.GONE);
        } else if (type == TYPE_TRENDING) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText(getEmptyMessage());
        }
    }

    private String getEmptyMessage() {
        switch (type) {
            case TYPE_SUBSCRIPTIONS: return "No subscriptions yet";
            case TYPE_LIBRARY: return "Your library is empty";
            default: return "No videos found";
        }
    }
}
