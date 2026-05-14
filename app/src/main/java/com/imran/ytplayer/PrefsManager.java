package com.imran.ytplayer;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PrefsManager {
    private static final String PREFS_NAME = "yt_prefs";
    private static final String KEY_API_KEY = "api_key";
    private static final String KEY_SIGNED_IN = "signed_in";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_HISTORY = "watch_history";
    private static final String KEY_PLAYLISTS = "saved_playlists";

    private SharedPreferences prefs;

    public PrefsManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void setApiKey(String key) {
        prefs.edit().putString(KEY_API_KEY, key).apply();
    }

    public String getApiKey() {
        return prefs.getString(KEY_API_KEY, "");
    }

    public void setSignedIn(boolean signedIn) {
        prefs.edit().putBoolean(KEY_SIGNED_IN, signedIn).apply();
    }

    public boolean isSignedIn() {
        return prefs.getBoolean(KEY_SIGNED_IN, false);
    }

    public void setUserName(String name) {
        prefs.edit().putString(KEY_USER_NAME, name).apply();
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    public void setUserEmail(String email) {
        prefs.edit().putString(KEY_USER_EMAIL, email).apply();
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    public void addToHistory(String videoId) {
        Set<String> history = new HashSet<>(prefs.getStringSet(KEY_HISTORY, new HashSet<>()));
        history.add(videoId);
        prefs.edit().putStringSet(KEY_HISTORY, history).apply();
    }

    public Set<String> getHistory() {
        return prefs.getStringSet(KEY_HISTORY, new HashSet<>());
    }

    public void addPlaylist(String playlistId) {
        Set<String> playlists = new HashSet<>(prefs.getStringSet(KEY_PLAYLISTS, new HashSet<>()));
        playlists.add(playlistId);
        prefs.edit().putStringSet(KEY_PLAYLISTS, playlists).apply();
    }

    public Set<String> getSavedPlaylists() {
        return prefs.getStringSet(KEY_PLAYLISTS, new HashSet<>());
    }

    public void clearAll() {
        prefs.edit().clear().apply();
    }
}
