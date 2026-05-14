package com.imran.ytplayer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PlayerActivity extends AppCompatActivity implements VideoAdapter.OnVideoClickListener {

    private PlayerView playerView;
    private ExoPlayer player;
    private TextView videoTitle, videoViews, videoDate, channelName, channelSubs, videoDescription;
    private ImageView channelAvatar;
    private RecyclerView relatedVideos;
    private VideoAdapter relatedAdapter;
    private YouTubeService youtubeService;
    private PrefsManager prefsManager;

    private String currentVideoId;
    private VideoItem currentVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        youtubeService = new YouTubeService(this);
        prefsManager = new PrefsManager(this);

        initViews();

        currentVideoId = getIntent().getStringExtra("video_id");
        if (currentVideoId == null) {
            Toast.makeText(this, "No video ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set title from intent if available
        String title = getIntent().getStringExtra("video_title");
        if (title != null) videoTitle.setText(title);

        loadVideoDetails();
        loadRelatedVideos();
    }

    private void initViews() {
        playerView = findViewById(R.id.player_view);
        videoTitle = findViewById(R.id.video_title);
        videoViews = findViewById(R.id.video_views);
        videoDate = findViewById(R.id.video_date);
        channelName = findViewById(R.id.channel_name);
        channelSubs = findViewById(R.id.channel_subs);
        videoDescription = findViewById(R.id.video_description);
        channelAvatar = findViewById(R.id.channel_avatar);
        relatedVideos = findViewById(R.id.related_videos);

        relatedVideos.setLayoutManager(new LinearLayoutManager(this));
        relatedAdapter = new VideoAdapter(this);
        relatedVideos.setAdapter(relatedAdapter);

        // Action buttons
        findViewById(R.id.btn_like).setOnClickListener(v -> Toast.makeText(this, "Liked!", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btn_dislike).setOnClickListener(v -> Toast.makeText(this, "Disliked", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btn_share).setOnClickListener(v -> shareVideo());
        findViewById(R.id.btn_download).setOnClickListener(v -> Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btn_subscribe).setOnClickListener(v -> Toast.makeText(this, "Subscribed!", Toast.LENGTH_SHORT).show());
    }

    private void initPlayer(String videoId) {
        if (player != null) {
            player.release();
        }

        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        // Use youtube.com/get_video_info or direct stream approach
        // For a real app, you'd use a YouTube extraction library
        // Here we use the YouTube embed URL approach
        String youtubeUrl = "https://www.youtube.com/watch?v=" + videoId;

        // Note: For actual playback, you need to extract the direct video URL
        // This requires a library like youtube-dl or NewPipe extractor
        // For now, we'll open in YouTube app as fallback
        try {
            DefaultHttpDataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory();
            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(youtubeUrl));
            player.setMediaItem(mediaItem);
            player.prepare();
            player.play();
        } catch (Exception e) {
            // Fallback: open in YouTube app
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrl));
                startActivity(intent);
            } catch (Exception ex) {
                Toast.makeText(this, "Cannot play video", Toast.LENGTH_SHORT).show();
            }
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

                        Glide.with(this)
                                .load(video.getThumbnailUrl())
                                .into(channelAvatar);

                        prefsManager.addToHistory(currentVideoId);
                        initPlayer(currentVideoId);
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    initPlayer(currentVideoId);
                });
            }
        }).start();
    }

    private void loadRelatedVideos() {
        new Thread(() -> {
            try {
                List<VideoItem> videos = youtubeService.searchVideos("related to " + currentVideoId, 10);
                runOnUiThread(() -> relatedAdapter.setVideos(videos));
            } catch (Exception e) {
                // Silently fail
            }
        }).start();
    }

    private String formatViews(String viewCount) {
        try {
            long views = Long.parseLong(viewCount);
            if (views >= 1_000_000) return String.format("%.1fM views", views / 1_000_000.0);
            if (views >= 1_000) return String.format("%.1fK views", views / 1_000.0);
            return views + " views";
        } catch (Exception e) {
            return viewCount + " views";
        }
    }

    private void shareVideo() {
        if (currentVideo != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "https://youtube.com/watch?v=" + currentVideoId);
            startActivity(Intent.createChooser(shareIntent, "Share video"));
        }
    }

    @Override
    public void onVideoClick(VideoItem video) {
        currentVideoId = video.getVideoId();
        currentVideo = video;
        videoTitle.setText(video.getTitle());
        channelName.setText(video.getChannelTitle());
        initPlayer(currentVideoId);
        loadVideoDetails();
    }

    @Override
    public void onVideoLongClick(VideoItem video) {
        onVideoClick(video);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.pause();
        }
    }
}
