package com.imran.ytplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchActivity extends AppCompatActivity implements VideoAdapter.OnVideoClickListener {

    private EditText searchInput;
    private ImageButton btnSearch, btnBack;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private View historySection;
    private ChipGroup historyChips;
    private VideoAdapter adapter;
    private YouTubeService youtubeService;
    private PrefsManager prefsManager;
    private Set<String> searchHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        youtubeService = new YouTubeService(this);
        prefsManager = new PrefsManager(this);

        // Safely load search history
        try {
            Set<String> history = prefsManager.getHistory();
            searchHistory = history != null ? new HashSet<>(history) : new HashSet<>();
        } catch (Exception e) {
            searchHistory = new HashSet<>();
        }

        initViews();
        showSearchHistory();
    }

    private void initViews() {
        searchInput = findViewById(R.id.search_input);
        btnSearch = findViewById(R.id.btn_search);
        btnBack = findViewById(R.id.btn_back);
        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        emptyView = findViewById(R.id.empty_view);
        historySection = findViewById(R.id.history_section);
        historyChips = findViewById(R.id.history_chips);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VideoAdapter(this);
        recyclerView.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        btnSearch.setOnClickListener(v -> performSearch());

        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });
    }

    private void showSearchHistory() {
        try {
            historyChips.removeAllViews();
            if (searchHistory == null || searchHistory.isEmpty()) {
                historySection.setVisibility(View.GONE);
                return;
            }

            historySection.setVisibility(View.VISIBLE);
            for (String query : searchHistory) {
                if (query == null || query.trim().isEmpty()) continue;
                Chip chip = new Chip(this);
                chip.setText(query);
                chip.setChipBackgroundColorResource(R.drawable.bg_chip);
                chip.setTextColor(0xFFFFFFFF);
                chip.setOnClickListener(v -> {
                    searchInput.setText(query);
                    performSearch();
                });
                historyChips.addView(chip);
            }
        } catch (Exception e) {
            // Prevent crash from bad history data
            historySection.setVisibility(View.GONE);
        }
    }

    private void performSearch() {
        String query = searchInput.getText().toString().trim();
        if (query.isEmpty()) return;

        // Hide keyboard
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
        } catch (Exception e) {
            // ignore
        }

        if (searchHistory == null) searchHistory = new HashSet<>();
        searchHistory.add(query);

        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        historySection.setVisibility(View.GONE);

        new Thread(() -> {
            try {
                List<VideoItem> results = youtubeService.searchVideos(query, 20);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (results == null || results.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                        emptyView.setText("No results found");
                    } else {
                        adapter.setVideos(results);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                    emptyView.setText("Error: " + e.getMessage());
                });
            }
        }).start();
    }

    @Override
    public void onVideoClick(VideoItem video) {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("video_id", video.getVideoId());
        intent.putExtra("video_title", video.getTitle());
        intent.putExtra("video_channel", video.getChannelTitle());
        startActivity(intent);
    }

    @Override
    public void onVideoLongClick(VideoItem video) {
        onVideoClick(video);
    }
}
