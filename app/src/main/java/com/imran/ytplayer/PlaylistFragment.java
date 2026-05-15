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

public class PlaylistFragment extends Fragment {

    private static final String TAG = "PlaylistFragment";
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private PlaylistAdapter adapter;
    private YouTubeService youtubeService;
    private PrefsManager prefsManager;

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
            prefsManager = new PrefsManager(getContext());
            // Set up the Google account credential so authenticated API calls work
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getContext());
            if (account != null) {
                youtubeService.setGoogleAccount(account);
            } else {
                Log.w(TAG, "No Google account found - playlists will be empty");
            }
        }

        adapter = new PlaylistAdapter(playlist -> {
            if (getContext() != null) {
                android.content.Intent intent = new android.content.Intent(getContext(), PlaylistActivity.class);
                intent.putExtra("playlist_id", playlist.getPlaylistId());
                intent.putExtra("playlist_title", playlist.getTitle());
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

        loadData();
        return view;
    }

    private void loadData() {
        if (youtubeService == null || prefsManager == null) return;

        if (!prefsManager.isSignedIn()) {
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText("Sign in to see your playlists");
            progressBar.setVisibility(View.GONE);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        new Thread(() -> {
            try {
                List<PlaylistItem> playlists = youtubeService.getUserPlaylists(20);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        if (playlists != null && !playlists.isEmpty()) {
                            adapter.setPlaylists(playlists);
                        } else {
                            emptyView.setVisibility(View.VISIBLE);
                            emptyView.setText("No playlists found");
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading playlists: " + e.getMessage(), e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        emptyView.setVisibility(View.VISIBLE);
                        emptyView.setText("Error loading playlists");
                    });
                }
            }
        }).start();
    }
}
