package com.imran.ytplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements VideoAdapter.OnVideoClickListener {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private YouTubeService youtubeService;
    private PrefsManager prefsManager;

    private List<VideoItem> trendingVideos = new ArrayList<>();
    private List<VideoItem> subscriptionVideos = new ArrayList<>();
    private List<PlaylistItem> playlists = new ArrayList<>();
    private List<VideoItem> libraryVideos = new ArrayList<>();

    private VideoAdapter trendingAdapter;
    private VideoAdapter subscriptionAdapter;
    private PlaylistAdapter playlistAdapter;
    private VideoAdapter libraryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        youtubeService = new YouTubeService(this);
        prefsManager = new PrefsManager(this);

        initViews();
        setupViewPager();
        loadTrendingVideos();
    }

    private void initViews() {
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);

        ImageButton btnSearch = findViewById(R.id.btn_search);
        ImageButton btnSettings = findViewById(R.id.btn_settings);
        FloatingActionButton fabAddUrl = findViewById(R.id.fab_add_url);

        btnSearch.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        fabAddUrl.setOnClickListener(v -> showUrlInputDialog());
    }

    private void setupViewPager() {
        MainPagerAdapter pagerAdapter = new MainPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Home"); break;
                case 1: tab.setText("Subscriptions"); break;
                case 2: tab.setText("Playlists"); break;
                case 3: tab.setText("Library"); break;
            }
        }).attach();
    }

    private void loadTrendingVideos() {
        new Thread(() -> {
            try {
                List<VideoItem> videos = youtubeService.getTrendingVideos(20);
                runOnUiThread(() -> {
                    trendingVideos.clear();
                    trendingVideos.addAll(videos);
                    if (trendingAdapter != null) {
                        trendingAdapter.setVideos(trendingVideos);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error loading videos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    public void refreshData() {
        loadTrendingVideos();
        if (prefsManager.isSignedIn()) {
            loadUserPlaylists();
        }
    }

    private void loadUserPlaylists() {
        new Thread(() -> {
            try {
                List<PlaylistItem> items = youtubeService.getUserPlaylists(20);
                runOnUiThread(() -> {
                    playlists.clear();
                    playlists.addAll(items);
                    if (playlistAdapter != null) {
                        playlistAdapter.setPlaylists(playlists);
                    }
                });
            } catch (Exception e) {
                // Silently fail for playlists
            }
        }).start();
    }

    private void showUrlInputDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_url_input, null);
        EditText urlInput = dialogView.findViewById(R.id.url_input);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_ok).setOnClickListener(v -> {
            String url = urlInput.getText().toString().trim();
            if (!url.isEmpty()) {
                handleUrl(url);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void handleUrl(String url) {
        String videoId = YouTubeService.extractVideoId(url);
        String playlistId = YouTubeService.extractPlaylistId(url);

        if (playlistId != null) {
            // Open playlist
            Intent intent = new Intent(this, PlaylistActivity.class);
            intent.putExtra("playlist_id", playlistId);
            intent.putExtra("playlist_title", "Playlist");
            startActivity(intent);
        } else if (videoId != null) {
            // Play video
            Intent intent = new Intent(this, PlayerActivity.class);
            intent.putExtra("video_id", videoId);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Invalid YouTube URL", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onVideoClick(VideoItem video) {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("video_id", video.getVideoId());
        intent.putExtra("video_title", video.getTitle());
        intent.putExtra("video_channel", video.getChannelTitle());
        intent.putExtra("video_thumbnail", video.getThumbnailUrl());
        startActivity(intent);
    }

    @Override
    public void onVideoLongClick(VideoItem video) {
        // Show options dialog
        String[] options = {"Play", "Add to queue", "Share"};
        new AlertDialog.Builder(this)
                .setTitle(video.getTitle())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: onVideoClick(video); break;
                        case 1: Toast.makeText(this, "Added to queue", Toast.LENGTH_SHORT).show(); break;
                        case 2: shareVideo(video); break;
                    }
                })
                .show();
    }

    private void shareVideo(VideoItem video) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "https://youtube.com/watch?v=" + video.getVideoId());
        startActivity(Intent.createChooser(shareIntent, "Share video"));
    }

    // Getters for adapters
    public VideoAdapter getTrendingAdapter() {
        if (trendingAdapter == null) {
            trendingAdapter = new VideoAdapter(this);
        }
        return trendingAdapter;
    }

    public VideoAdapter getSubscriptionAdapter() {
        if (subscriptionAdapter == null) {
            subscriptionAdapter = new VideoAdapter(this);
        }
        return subscriptionAdapter;
    }

    public PlaylistAdapter getPlaylistAdapter() {
        if (playlistAdapter == null) {
            playlistAdapter = new PlaylistAdapter(playlist -> {
                Intent intent = new Intent(this, PlaylistActivity.class);
                intent.putExtra("playlist_id", playlist.getPlaylistId());
                intent.putExtra("playlist_title", playlist.getTitle());
                startActivity(intent);
            });
        }
        return playlistAdapter;
    }

    public VideoAdapter getLibraryAdapter() {
        if (libraryAdapter == null) {
            libraryAdapter = new VideoAdapter(this);
        }
        return libraryAdapter;
    }

    public List<VideoItem> getTrendingVideos() { return trendingVideos; }
    public List<VideoItem> getSubscriptionVideos() { return subscriptionVideos; }
    public List<PlaylistItem> getPlaylists() { return playlists; }
    public List<VideoItem> getLibraryVideos() { return libraryVideos; }
}
