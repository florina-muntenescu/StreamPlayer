package com.android.magic.stream.player;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;


/**
 * Player thread - handles the {@link android.media.MediaPlayer} instance and all the notifications
 * related to it
 */
/*default*/ class MediaPlayerThread extends Thread implements OnBufferingUpdateListener,
        OnPreparedListener, OnErrorListener {

    private static final String LOG_TAG = MediaPlayerThread.class.getSimpleName();

    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private IMediaPlayerThreadClient mClient;
    private WifiManager.WifiLock mWifiLock;

    public MediaPlayerThread(IMediaPlayerThreadClient client, Context context) {
        mClient = client;
        mWifiLock = ((WifiManager) context.getApplicationContext().getSystemService(
                Context.WIFI_SERVICE)).createWifiLock(
                WifiManager.WIFI_MODE_FULL, "streamplayer.lock");
    }

    /**
     * Initializes a StatefulMediaPlayer for streaming playback of the provided Radio
     *
     * @param station The url representing the station to play
     */
    public void initializePlayer(final String station) {

        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.reset();
        }
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mMediaPlayer.setDataSource(station);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnBufferingUpdateListener(this);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.prepareAsync();

            mWifiLock.acquire();

        } catch (IOException e) {

        }
    }


    /**
     * Starts the contained StatefulMediaPlayer and foregrounds the service to support
     * persisted background playback.
     */
    public void startMediaPlayer() {
        Log.d("MediaPlayerThread", "startMediaPlayer() called");

        mMediaPlayer.start();
        mWifiLock.acquire();

        mClient.onPlaying();
    }

    /**
     * Pauses the contained StatefulMediaPlayer
     */
    public void pauseMediaPlayer() {
        Log.d("MediaPlayerThread", "pauseMediaPlayer() called");
        mMediaPlayer.pause();

        mClient.onStop();
        mWifiLock.release();

    }

    /**
     * Stops the contained StatefulMediaPlayer.
     */
    public void stopMediaPlayer() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        mClient.onStop();

        if (mWifiLock.isHeld()) {
            mWifiLock.release();
        }
    }

    public void resetMediaPlayer() {
        mMediaPlayer.reset();
    }

    @Override
    public void onBufferingUpdate(MediaPlayer player, int percent) {
        Log.d(LOG_TAG, "buffering " + percent);
    }

    @Override
    public boolean onError(MediaPlayer player, int what, int extra) {
        Log.d(LOG_TAG, "onError " + what + " " + extra);

        mMediaPlayer.reset();
        mClient.onError(what, extra);
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer player) {
        startMediaPlayer();
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

}
