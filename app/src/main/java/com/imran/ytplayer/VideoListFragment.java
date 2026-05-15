package com.imran.ytplayer;

import android.os.Bundle;
import android.util.Log;
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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.util.List;

public class VideoListFragment extends Fragment {

    private static final String ARG_TYPE = "type";
    private static final String TAG = "VideoListFragment";
    public static final int TYPE_SUBSCRIPTIONS = 1;

    private int type;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private VideoAdapter adapter;
    private YouTubeService youtubeService;

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

        if (getContext() != null) {
            youtubeService = new YouTubeService(getContext());
            // Set up the Google account credential so authenticated API calls work
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getContext());
            if (account != null) {
                youtubeService.setGoogleAccount(account);
            }
        }

        adapter = new VideoAdapter(new VideoAdapter.OnVideoClickListener() {
            @Override
            public void onVideoClick(VideoItem video) {
                if (getContext() != null) {
                    android.content.Intent intent = new android.content.Intent(getContext(), PlayerActivity.class);
                    intent.putExtra("video_id", video.getVideoId());
                    intent.putExtra("video_title", video.getTitle());
                    intent.putExtra("video_channel", video.getChannelTitle());
                    startActivity(intent);
                }
            }

            @Override
            public void onVideoLongClick(VideoItem video) {
                onVideoClick(video);
            }
        });
        recyclerView.setAdapter(adapter);

        loadData();
        return view;
    }

    private void loadData() {
        if (type == TYPE_SUBSCRIPTIONS) {
            if (youtubeService == null) {
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText("Sign in to see subscriptions");
                progressBar.setVisibility(View.GONE);
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            new Thread(() -> {
                try {
                    List<VideoItem> videos = youtubeService.getSubscriptionVideos(20);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            if (videos != null && !videos.isEmpty()) {
                                adapter.setVideos(videos);
                                emptyView.setVisibility(View.GONE);
                            } else {
                                emptyView.setVisibility(View.VISIBLE);
                                emptyView.setText("No subscription videos found. Subscribe to channels on YouTube to see their latest videos here.");
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error loading subscriptions: " + e.getMessage(), e);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                            emptyView.setText("Sign in to see subscriptions");
                        });
                    }
                }
            }).start();
        }
    }
}
