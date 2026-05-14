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

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    public interface OnPlaylistClickListener {
        void onPlaylistClick(PlaylistItem playlist);
    }

    private List<PlaylistItem> playlists = new ArrayList<>();
    private OnPlaylistClickListener listener;

    public PlaylistAdapter(OnPlaylistClickListener listener) {
        this.listener = listener;
    }

    public void setPlaylists(List<PlaylistItem> playlists) {
        this.playlists = playlists != null ? playlists : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_playlist, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        PlaylistItem playlist = playlists.get(position);
        holder.title.setText(playlist.getTitle());
        holder.channel.setText(playlist.getChannelTitle());
        holder.count.setText(playlist.getVideoCount() + " videos");

        Glide.with(holder.thumbnail.getContext())
                .load(playlist.getThumbnailUrl())
                .placeholder(R.drawable.ic_video_placeholder)
                .error(R.drawable.ic_video_placeholder)
                .centerCrop()
                .into(holder.thumbnail);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPlaylistClick(playlist);
        });
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView title, channel, count;

        PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.playlist_thumbnail);
            title = itemView.findViewById(R.id.playlist_title);
            channel = itemView.findViewById(R.id.playlist_channel);
            count = itemView.findViewById(R.id.playlist_count);
        }
    }
}
