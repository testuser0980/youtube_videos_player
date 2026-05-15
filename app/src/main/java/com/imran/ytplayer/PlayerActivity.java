package com.imran.ytplayer;

import android.content.Intent;
import android.os.Bundle;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.List;

public class PlayerActivity extends AppCompatActivity implements VideoAdapter.OnVideoClickListener {

    private YouTubePlayerView youTubePlayerView;
    private TextView videoTitle, videoViews, videoDate, channelName, channelSubs, videoDescription;
    private ImageView channelAvatar;
    private RecyclerView relatedVideos;
    private VideoAdapter relatedAdapter;
    private YouTubeService youtubeService;
    private PrefsManager prefsManager;

    private String currentVideoId;
    private VideoItem currentVideo;
    private YouTubePlayer youTubePlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        youtubeService = new YouTubeService(this);
        prefsManager = new PrefsManager(this);

        currentVideoId = getIntent().getStringExtra("video_id");
        if (currentVideoId == null) {
            Toast.makeText(this, "No video ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();

        String title = getIntent().getStringExtra("video_title");
        if (title != null) videoTitle.setText(title);

        loadVideoDetails();
        loadRelatedVideos();
    }

    private void initViews() {
        youTubePlayerView = findViewById(R.id.youtube_player_view);
        videoTitle = findViewById(R.id.video_title);
        videoViews = findViewById(R.id.video_views);
        videoDate = findViewById(R.id.video_date);
        channelName = findViewById(R.id.channel_name);
        channelSubs = findViewById(R.id.channel_subs);
        videoDescription = findViewById(R.id.video_description);
        channelAvatar = findViewById(R.id.channel_avatar);
        relatedVideos = findViewById(R.id.related_videos);

        getLifecycle().addObserver(youTubePlayerView);

        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer player) {
                youTubePlayer = player;
                player.loadVideo(currentVideoId, 0);
            }
        });

        relatedVideos.setLayoutManager(new LinearLayoutManager(this));
        relatedAdapter = new VideoAdapter(this);
        relatedVideos.setAdapter(relatedAdapter);

        findViewById(R.id.btn_like).setOnClickListener(v -> Toast.makeText(this, "Liked!", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btn_dislike).setOnClickListener(v -> Toast.makeText(this, "Disliked", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btn_download).setOnClickListener(v -> Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btn_subscribe).setOnClickListener(v -> Toast.makeText(this, "Subscribed!", Toast.LENGTH_SHORT).show());

        // Fullscreen toggle
        ImageButton btnFullscreen = findViewById(R.id.btn_fullscreen);
        if (btnFullscreen != null) {
            btnFullscreen.setOnClickListener(v -> toggleFullscreen());
        }
    }

    private boolean isFullscreen = false;

    private void toggleFullscreen() {
        if (isFullscreen) {
            // Exit fullscreen
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            isFullscreen = false;
        } else {
            // Enter fullscreen (landscape)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            isFullscreen = true;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    private void loadVideoDetails() {
        new Thread(() -> {
            try {
                VideoItem video = youtubeService.getVideoDetails(currentVideoId);
                if (video != null) {
                    currentVideo = video;
                    runOnUiThread(() -> {
                        videoTitle.setText(video.getTitle());
                        channelName.setText(video.getChannelTitle());
                        videoDescription.setText(video.getDescription());
                        videoViews.setText(formatViews(video.getViewCount()));
                        videoDate.setText(video.getPublishedAt());
                        channelSubs.setText("Subscribers");
                        Glide.with(PlayerActivity.this).load(video.getThumbnailUrl()).into(channelAvatar);
                        prefsManager.addToHistory(currentVideoId);
                    });
                }
            } catch (Exception e) {
                // Silently fail
            }
        }).start();
    }

    private void loadRelatedVideos() {
        new Thread(() -> {
            try {
                List<VideoItem> videos = youtubeService.searchVideos("related to " + currentVideoId, 10);
                runOnUiThread(() -> {
                    if (videos != null) relatedAdapter.setVideos(videos);
                });
            } catch (Exception e) {
                // Silently fail
            }
        }).start();
    }

    private String formatViews(String viewCount) {
        if (viewCount == null || viewCount.isEmpty()) return "";
        try {
            long views = Long.parseLong(viewCount);
            if (views >= 1_000_000) return String.format("%.1fM views", views / 1_000_000.0);
            if (views >= 1_000) return String.format("%.1fK views", views / 1_000.0);
            return views + " views";
        } catch (Exception e) {
            return viewCount + " views";
        }
    }

    @Override
    public void onVideoClick(VideoItem video) {
        currentVideoId = video.getVideoId();
        currentVideo = video;
        videoTitle.setText(video.getTitle());
        channelName.setText(video.getChannelTitle());

        if (youTubePlayer != null) {
            youTubePlayer.loadVideo(currentVideoId, 0);
        }

        loadVideoDetails();
    }

    @Override
    public void onVideoLongClick(VideoItem video) {
        onVideoClick(video);
    }
}
