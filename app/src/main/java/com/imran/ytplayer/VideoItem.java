package com.imran.ytplayer;

import java.io.Serializable;
import java.util.List;

public class VideoItem implements Serializable {
    private String videoId;
    private String title;
    private String channelTitle;
    private String thumbnailUrl;
    private String duration;
    private String viewCount;
    private String publishedAt;
    private String description;
    private boolean isPlaylistItem;

    public VideoItem() {}

    public VideoItem(String videoId, String title, String channelTitle, String thumbnailUrl) {
        this.videoId = videoId;
        this.title = title;
        this.channelTitle = channelTitle;
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getVideoId() { return videoId; }
    public void setVideoId(String videoId) { this.videoId = videoId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getChannelTitle() { return channelTitle; }
    public void setChannelTitle(String channelTitle) { this.channelTitle = channelTitle; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getDuration() { return duration != null ? duration : ""; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getViewCount() { return viewCount != null ? viewCount : ""; }
    public void setViewCount(String viewCount) { this.viewCount = viewCount; }

    public String getPublishedAt() { return publishedAt != null ? publishedAt : ""; }
    public void setPublishedAt(String publishedAt) { this.publishedAt = publishedAt; }

    public String getDescription() { return description != null ? description : ""; }
    public void setDescription(String description) { this.description = description; }

    public boolean isPlaylistItem() { return isPlaylistItem; }
    public void setPlaylistItem(boolean playlistItem) { isPlaylistItem = playlistItem; }
}
