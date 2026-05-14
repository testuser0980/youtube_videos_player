package com.imran.ytplayer;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlaylistActivity extends AppCompatActivity implements VideoAdapter.OnVideoClickListener {

    private TextView playlistTitle, playlistCount;
    private ImageButton btnBack;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private VideoAdapter adapter;
    private YouTubeService youtubeService;

    private String playlistId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        youtubeService = new YouTubeService(this);

        playlistId = getIntent().getStringExtra("playlist_id");
        String title = getIntent().getStringExtra("playlist_title");

        initViews(title);
        loadPlaylistVideos();
    }

    private void initViews(String title) {
        playlistTitle = findViewById(R.id.playlist_title);
        playlistCount = findViewById(R.id.playlist_count);
        btnBack = findViewById(R.id.btn_back);
        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);

        if (title != null) playlistTitle.setText(title);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VideoAdapter(this);
        recyclerView.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadPlaylistVideos() {
        if (playlistId == null) {
            Toast.makeText(this, "No playlist ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                List<VideoItem> videos = youtubeService.getPlaylistVideos(playlistId, 50);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    playlistCount.setText(videos.size() + " videos");
                    adapter.setVideos(videos);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading playlist: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    @Override
    public void onVideoClick(VideoItem video) {
        if (video.getVideoId() != null) {
            android.content.Intent intent = new android.content.Intent(this, PlayerActivity.class);
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
}
