package com.imran.ytplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements VideoAdapter.OnVideoClickListener {

    private static final int RC_SETTINGS = 2001;

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private YouTubeService youtubeService;
    private PrefsManager prefsManager;

    private List<PlaylistItem> playlists = new ArrayList<>();
    private List<VideoItem> subscriptionVideos = new ArrayList<>();
    private PlaylistAdapter playlistAdapter;
    private VideoAdapter subscriptionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        youtubeService = new YouTubeService(this);
        prefsManager = new PrefsManager(this);

        initViews();
        setupViewPager();

        // Check if already signed in and load data
        if (prefsManager.isSignedIn()) {
            loadUserPlaylists();
        }
    }

    private void initViews() {
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);

        ImageButton btnSearch = findViewById(R.id.btn_search);
        ImageButton btnSettings = findViewById(R.id.btn_settings);
        FloatingActionButton fabAddUrl = findViewById(R.id.fab_add_url);

        btnSearch.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        btnSettings.setOnClickListener(v -> startActivityForResult(new Intent(this, SettingsActivity.class), RC_SETTINGS));
        fabAddUrl.setOnClickListener(v -> showUrlInputDialog());
    }

    private void setupViewPager() {
        MainPagerAdapter pagerAdapter = new MainPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Playlists"); break;
                case 1: tab.setText("Subscriptions"); break;
            }
        }).attach();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SETTINGS && resultCode == RESULT_OK && data != null) {
            // User signed in, set up credential for YouTube API
            String email = data.getStringExtra("account_email");
            if (email != null) {
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
                if (account != null) {
                    youtubeService.setGoogleAccount(account);
                }
            }
            loadUserPlaylists();
        }
    }

    private void loadUserPlaylists() {
        if (!prefsManager.isSignedIn()) return;

        new Thread(() -> {
            try {
                List<PlaylistItem> items = youtubeService.getUserPlaylists(20);
                runOnUiThread(() -> {
                    playlists.clear();
                    if (items != null) playlists.addAll(items);
                    if (playlistAdapter != null) playlistAdapter.setPlaylists(playlists);
                });
            } catch (Exception e) {
                // Silently fail
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
            Intent intent = new Intent(this, PlaylistActivity.class);
            intent.putExtra("playlist_id", playlistId);
            intent.putExtra("playlist_title", "Playlist");
            startActivity(intent);
        } else if (videoId != null) {
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

    public VideoAdapter getSubscriptionAdapter() {
        if (subscriptionAdapter == null) {
            subscriptionAdapter = new VideoAdapter(this);
        }
        return subscriptionAdapter;
    }

    public List<PlaylistItem> getPlaylists() { return playlists; }
    public List<VideoItem> getSubscriptionVideos() { return subscriptionVideos; }
}
