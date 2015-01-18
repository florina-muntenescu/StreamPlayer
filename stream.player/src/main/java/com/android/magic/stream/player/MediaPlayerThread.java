package com.android.magic.stream.player;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;


/**
 * Player thread
 */
/*default*/ class MediaPlayerThread extends Thread implements OnBufferingUpdateListener,
        OnInfoListener, OnPreparedListener, OnErrorListener {

    private static final String LOG_TAG = MediaPlayerThread.class.getSimpleName();

    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private IMediaPlayerThreadClient mClient;
    private WifiManager.WifiLock mWifiLock;
    private Context mContext;

    public MediaPlayerThread(IMediaPlayerThreadClient client, Context context) {
        mClient = client;
        mContext = context;
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
            mMediaPlayer.setOnBufferingUpdateListener(this);
            mMediaPlayer.setOnInfoListener(this);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.prepareAsync();

            mWifiLock.acquire();

        }catch (IOException e){

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
        //        if(mMediaPlayer.isPlaying()) {
        //            mMediaPlayer.stop();
        //        }
        mMediaPlayer.release();
        mMediaPlayer = null;

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
        mMediaPlayer.reset();
        mClient.onError();
        return true;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer player) {
        Log.d(LOG_TAG, "prepared ");
        startMediaPlayer();
    }

    /**
     * @return
     */
    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

}
