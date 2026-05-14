package com.imran.ytplayer;

import java.io.Serializable;
import java.util.List;

public class PlaylistItem implements Serializable {
    private String playlistId;
    private String title;
    private String channelTitle;
    private String thumbnailUrl;
    private String videoCount;
    private String description;

    public PlaylistItem() {}

    public PlaylistItem(String playlistId, String title, String channelTitle, String thumbnailUrl, String videoCount) {
        this.playlistId = playlistId;
        this.title = title;
        this.channelTitle = channelTitle;
        this.thumbnailUrl = thumbnailUrl;
        this.videoCount = videoCount;
    }

    public String getPlaylistId() { return playlistId; }
    public void setPlaylistId(String playlistId) { this.playlistId = playlistId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getChannelTitle() { return channelTitle; }
    public void setChannelTitle(String channelTitle) { this.channelTitle = channelTitle; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getVideoCount() { return videoCount != null ? videoCount : "0"; }
    public void setVideoCount(String videoCount) { this.videoCount = videoCount; }

    public String getDescription() { return description != null ? description : ""; }
    public void setDescription(String description) { this.description = description; }
}
