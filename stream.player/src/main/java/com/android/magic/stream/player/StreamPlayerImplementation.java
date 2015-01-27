package com.android.magic.stream.player;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Handles the playing, stopping and pausing of url streams, notifying observers of changes in
 * stream playing or track changing
 */
/*package*/ class StreamPlayerImplementation implements StreamPlayer {

    private static final String LOG_TAG = StreamPlayerImplementation.class.getSimpleName();

    private StreamPlayerListener mStreamPlayerListener;
    private TrackListener mTrackListener;

    private MediaPlayerService mService;
    private boolean mBound;

    private Context mContext;

    private String mURL;

    StreamPlayerImplementation(Context context) {
        mContext = context;
        bindToService();
    }

    /**
     * Plays the stream
     *
     * @param url the stream that will be played
     */
    @Override
    public void play(@NonNull String url) {
        // if it's paused just start it
        if (mURL != null && mURL.equals(url) && !mService.isStopped() && !mService.isPlaying()) {
            mService.startMediaPlayer();
        } else {
            // need to initialize again the player
            mURL = url;
            mService.initializePlayer(url);
        }
    }

    /**
     * Pauses the currently playing stream
     */
    @Override
    public void pause() {
        if (mService.getMediaPlayer() != null && mService.getMediaPlayer().isPlaying()) {
            mService.pauseMediaPlayer();
        }
    }

    /**
     * Stops the currently playing stream
     */
    @Override
    public void stop() {
        if (mService.getMediaPlayer() != null && mService.getMediaPlayer().isPlaying()) {
            mService.stopMediaPlayer();
        }
    }

    /**
     * Closes unbinds from service, stops the service, and calls finish()
     */
    @Override
    public void shutDown() {
        if (mBound) {
            mService.stopMediaPlayer();
            mService.removeListener();
            // Detach existing connection.
            mContext.unbindService(mConnection);
            mBound = false;
        }

        Intent intent = new Intent(mContext, MediaPlayerService.class);
        mContext.stopService(intent);

    }

    /**
     * Retrieves the playing URL or null if there's no url currently playing
     *
     * @return the url of the radio playing or null
     */
    @Override
    public String getPlayingUrl() {
        if (mService != null && mService.isPlaying()) {
            return mService.getURL();
        }
        return null;
    }

    /**
     * Register a listener to be notified about player states
     */
    @Override
    public void registerStreamPlayerListener(StreamPlayerListener listener) {
        mStreamPlayerListener = listener;
        if (mService != null) {
            mService.addListener(listener);
        }
    }

    /**
     * Unregister the {@link com.android.magic.stream.player.StreamPlayerListener}
     */
    @Override
    public void unregisterStreamPlayerListener() {
        mService.removeListener();
        mStreamPlayerListener = null;
    }

    /**
     * Register a listener to be notified about track changes for the current stream
     */
    @Override
    public void registerTrackListener(TrackListener listener) {
        mTrackListener = listener;
        if (mService != null) {
            mService.addTrackListener(listener);
        }
    }

    /**
     * Unregister the track listener
     */
    @Override
    public void unregisterTrackListener() {
        mService.stopRetrievingMetadata();
    }


    /**
     * Binds to the instance of MediaPlayerService. If no instance of MediaPlayerService exists,
     * it first starts
     * a new instance of the service.
     */
    private void bindToService() {
        Intent intent = new Intent(mContext, MediaPlayerService.class);

        if (mediaPlayerServiceRunning()) {
            Log.d(LOG_TAG, "player running");
            // Bind to Service
            mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
        //no instance of service
        else {
            Log.d(LOG_TAG, "new instance of service");
            //start service and bind to it
            mContext.startService(intent);
            mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        }

    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder serviceBinder) {
            Log.d("MainActivity", "service connected");

            //bound with Service. get Service instance
            MediaPlayerService.MediaPlayerBinder binder = (MediaPlayerService.MediaPlayerBinder)
                    serviceBinder;
            mService = binder.getService();

            //send this instance to the service, so it can make callbacks on this instance as a
            // client
            mBound = true;
            mService.addListener(mStreamPlayerListener);
            if (mTrackListener != null) {
                mService.addTrackListener(mTrackListener);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            mService = null;
        }
    };

    /**
     * Determines if the MediaPlayerService is already running.
     *
     * @return true if the service is running, false otherwise.
     */
    private boolean mediaPlayerServiceRunning() {
        ActivityManager manager = (ActivityManager) mContext.getSystemService(
                mContext.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (MediaPlayerService.MEDIA_PLAYER_SERVICE.equals(service.service.getClassName())) {
                return true;
            }
        }

        return false;
    }


    public void disconnect() {
        if (mBound) {
            mService.unRegister();
            mContext.unbindService(mConnection);
            mBound = false;
        }
    }
}
