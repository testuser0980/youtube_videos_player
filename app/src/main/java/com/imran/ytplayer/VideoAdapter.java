package com.imran.ytplayer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    public interface OnVideoClickListener {
        void onVideoClick(VideoItem video);
        void onVideoLongClick(VideoItem video);
    }

    private List<VideoItem> videos = new ArrayList<>();
    private OnVideoClickListener listener;

    public VideoAdapter(OnVideoClickListener listener) {
        this.listener = listener;
    }

    public void setVideos(List<VideoItem> videos) {
        this.videos = videos != null ? videos : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addVideos(List<VideoItem> newVideos) {
        if (newVideos != null) {
            int start = videos.size();
            videos.addAll(newVideos);
            notifyItemRangeInserted(start, newVideos.size());
        }
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoItem video = videos.get(position);
        holder.title.setText(video.getTitle());
        holder.channel.setText(video.getChannelTitle());
        holder.meta.setText(formatMeta(video));

        Glide.with(holder.thumbnail.getContext())
                .load(video.getThumbnailUrl())
                .placeholder(R.drawable.ic_video_placeholder)
                .error(R.drawable.ic_video_placeholder)
                .centerCrop()
                .into(holder.thumbnail);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onVideoClick(video);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onVideoLongClick(video);
            return true;
        });
    }

    private String formatMeta(VideoItem video) {
        StringBuilder sb = new StringBuilder();
        if (video.getViewCount() != null && !video.getViewCount().isEmpty()) {
            try {
                long views = Long.parseLong(video.getViewCount());
                sb.append(formatViews(views)).append(" views");
            } catch (NumberFormatException e) {
                // ignore invalid view count
            }
        }
        if (video.getPublishedAt() != null && !video.getPublishedAt().isEmpty()) {
            if (sb.length() > 0) sb.append(" • ");
            sb.append(formatDate(video.getPublishedAt()));
        }
        return sb.toString();
    }

    private String formatViews(long views) {
        if (views >= 1_000_000) return String.format("%.1fM", views / 1_000_000.0);
        if (views >= 1_000) return String.format("%.1fK", views / 1_000.0);
        return String.valueOf(views);
    }

    private String formatDate(String dateStr) {
        try {
            // Simple date formatting - extract relative time
            return "Recently";
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView title, channel, meta;

        VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.video_thumbnail);
            title = itemView.findViewById(R.id.video_title);
            channel = itemView.findViewById(R.id.video_channel);
            meta = itemView.findViewById(R.id.video_meta);
        }
    }
}
