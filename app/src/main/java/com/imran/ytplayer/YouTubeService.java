package com.imran.ytplayer;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class YouTubeService {
    private static final String API_KEY = "AIzaSyDummy_ReplaceWithRealKey";
    private static final String APP_NAME = "YT Player";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    private YouTube youtube;
    private GoogleAccountCredential credential;
    private Context context;

    public YouTubeService(Context context) {
        this.context = context;
        this.youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, request -> {})
                .setApplicationName(APP_NAME)
                .build();
    }

    public void setCredential(GoogleAccountCredential credential) {
        this.credential = credential;
        this.youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APP_NAME)
                .build();
    }

    public List<VideoItem> searchVideos(String query, int maxResults) throws IOException {
        YouTube.Search.List search = youtube.search().list("id,snippet");
        search.setKey(getApiKey());
        search.setQ(query);
        search.setType("video");
        search.setMaxResults((long) maxResults);
        search.setPart("snippet");

        SearchListResponse response = search.execute();
        List<SearchResult> results = response.getItems();
        List<VideoItem> videos = new ArrayList<>();

        if (results != null) {
            for (SearchResult result : results) {
                VideoItem item = new VideoItem();
                if (result.getId() != null && result.getId().getVideoId() != null) {
                    item.setVideoId(result.getId().getVideoId());
                }
                if (result.getSnippet() != null) {
                    item.setTitle(result.getSnippet().getTitle());
                    item.setChannelTitle(result.getSnippet().getChannelTitle());
                    if (result.getSnippet().getThumbnails() != null &&
                            result.getSnippet().getThumbnails().getMedium() != null) {
                        item.setThumbnailUrl(result.getSnippet().getThumbnails().getMedium().getUrl());
                    }
                    item.setPublishedAt(result.getSnippet().getPublishedAt().toString());
                    item.setDescription(result.getSnippet().getDescription());
                }
                videos.add(item);
            }
        }
        return videos;
    }

    public List<VideoItem> getTrendingVideos(int maxResults) throws IOException {
        YouTube.Videos.List request = youtube.videos().list("id,snippet,contentDetails,statistics");
        request.setKey(getApiKey());
        request.setChart("mostPopular");
        request.setRegionCode("US");
        request.setMaxResults((long) maxResults);
        request.setPart("snippet,contentDetails,statistics");

        VideoListResponse response = request.execute();
        List<Video> results = response.getItems();
        List<VideoItem> videos = new ArrayList<>();

        if (results != null) {
            for (Video video : results) {
                VideoItem item = new VideoItem();
                item.setVideoId(video.getId());
                if (video.getSnippet() != null) {
                    item.setTitle(video.getSnippet().getTitle());
                    item.setChannelTitle(video.getSnippet().getChannelTitle());
                    if (video.getSnippet().getThumbnails() != null &&
                            video.getSnippet().getThumbnails().getMedium() != null) {
                        item.setThumbnailUrl(video.getSnippet().getThumbnails().getMedium().getUrl());
                    }
                    item.setPublishedAt(video.getSnippet().getPublishedAt().toString());
                    item.setDescription(video.getSnippet().getDescription());
                }
                if (video.getContentDetails() != null) {
                    item.setDuration(video.getContentDetails().getDuration());
                }
                if (video.getStatistics() != null) {
                    item.setViewCount(video.getStatistics().getViewCount().toString());
                }
                videos.add(item);
            }
        }
        return videos;
    }

    public List<VideoItem> getPlaylistVideos(String playlistId, int maxResults) throws IOException {
        YouTube.PlaylistItems.List request = youtube.playlistItems().list("id,snippet,contentDetails");
        request.setKey(getApiKey());
        request.setPlaylistId(playlistId);
        request.setMaxResults((long) maxResults);
        request.setPart("snippet,contentDetails");

        com.google.api.services.youtube.model.PlaylistItemListResponse response = request.execute();
        List<com.google.api.services.youtube.model.PlaylistItem> results = response.getItems();
        List<VideoItem> videos = new ArrayList<>();

        if (results != null) {
            for (com.google.api.services.youtube.model.PlaylistItem pi : results) {
                VideoItem item = new VideoItem();
                if (pi.getContentDetails() != null) {
                    item.setVideoId(pi.getContentDetails().getVideoId());
                }
                if (pi.getSnippet() != null) {
                    item.setTitle(pi.getSnippet().getTitle());
                    item.setChannelTitle(pi.getSnippet().getChannelTitle());
                    if (pi.getSnippet().getThumbnails() != null &&
                            pi.getSnippet().getThumbnails().getMedium() != null) {
                        item.setThumbnailUrl(pi.getSnippet().getThumbnails().getMedium().getUrl());
                    }
                    item.setPlaylistItem(true);
                }
                videos.add(item);
            }
        }
        return videos;
    }

    public List<PlaylistItem> getUserPlaylists(int maxResults) throws IOException {
        if (credential == null) return new ArrayList<>();

        YouTube.Playlists.List request = youtube.playlists().list("id,snippet,contentDetails");
        request.setMaxResults((long) maxResults);
        request.setMine(true);
        request.setPart("snippet,contentDetails");

        PlaylistListResponse response = request.execute();
        List<Playlist> results = response.getItems();
        List<PlaylistItem> playlists = new ArrayList<>();

        if (results != null) {
            for (Playlist p : results) {
                PlaylistItem item = new PlaylistItem();
                item.setPlaylistId(p.getId());
                if (p.getSnippet() != null) {
                    item.setTitle(p.getSnippet().getTitle());
                    item.setChannelTitle(p.getSnippet().getChannelTitle());
                    if (p.getSnippet().getThumbnails() != null &&
                            p.getSnippet().getThumbnails().getMedium() != null) {
                        item.setThumbnailUrl(p.getSnippet().getThumbnails().getMedium().getUrl());
                    }
                    item.setDescription(p.getSnippet().getDescription());
                }
                if (p.getContentDetails() != null) {
                    item.setVideoCount(p.getContentDetails().getItemCount().toString());
                }
                playlists.add(item);
            }
        }
        return playlists;
    }

    public VideoItem getVideoDetails(String videoId) throws IOException {
        YouTube.Videos.List request = youtube.videos().list("id,snippet,contentDetails,statistics");
        request.setKey(getApiKey());
        request.setId(videoId);
        request.setPart("snippet,contentDetails,statistics");

        VideoListResponse response = request.execute();
        List<Video> results = response.getItems();

        if (results != null && !results.isEmpty()) {
            Video video = results.get(0);
            VideoItem item = new VideoItem();
            item.setVideoId(video.getId());
            if (video.getSnippet() != null) {
                item.setTitle(video.getSnippet().getTitle());
                item.setChannelTitle(video.getSnippet().getChannelTitle());
                if (video.getSnippet().getThumbnails() != null &&
                        video.getSnippet().getThumbnails().getHigh() != null) {
                    item.setThumbnailUrl(video.getSnippet().getThumbnails().getHigh().getUrl());
                }
                item.setDescription(video.getSnippet().getDescription());
                item.setPublishedAt(video.getSnippet().getPublishedAt().toString());
            }
            if (video.getContentDetails() != null) {
                item.setDuration(video.getContentDetails().getDuration());
            }
            if (video.getStatistics() != null) {
                item.setViewCount(video.getStatistics().getViewCount().toString());
            }
            return item;
        }
        return null;
    }

    private String getApiKey() {
        SharedPreferences prefs = context.getSharedPreferences("yt_prefs", Context.MODE_PRIVATE);
        return prefs.getString("api_key", API_KEY);
    }

    public static String extractVideoId(String url) {
        if (url == null) return null;
        // Handle various YouTube URL formats
        String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\\/|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";
        java.util.regex.Pattern compiledPattern = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher matcher = compiledPattern.matcher(url);
        if (matcher.find()) {
            return matcher.group();
        }
        // Simple fallback
        if (url.contains("v=")) {
            int start = url.indexOf("v=") + 2;
            int end = url.indexOf("&", start);
            if (end == -1) end = url.length();
            return url.substring(start, end);
        }
        if (url.contains("youtu.be/")) {
            int start = url.indexOf("youtu.be/") + 9;
            int end = url.indexOf("?", start);
            if (end == -1) end = url.length();
            return url.substring(start, end);
        }
        return null;
    }

    public static String extractPlaylistId(String url) {
        if (url == null) return null;
        if (url.contains("list=")) {
            int start = url.indexOf("list=") + 5;
            int end = url.indexOf("&", start);
            if (end == -1) end = url.length();
            return url.substring(start, end);
        }
        return null;
    }
}
