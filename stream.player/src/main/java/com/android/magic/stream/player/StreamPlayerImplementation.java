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

    @Override
    public void play(@NonNull String url) {
        // if it's paused
        if (mURL != null && mURL.equals(url) && !mService.isStopped() && !mService.isPlaying()) {
            mService.startMediaPlayer();
        } else {
            newRadioSelected(url);
        }
    }

    private void newRadioSelected(String url) {
        mURL = url;
        mService.initializePlayer(url);
    }

    @Override
    public void pause() {
        if (mService.getMediaPlayer().isPlaying()) {
            mService.pauseMediaPlayer();
        }
    }

    @Override
    public void stop() {
        if (mService.getMediaPlayer().isPlaying()) {
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
            // Detach existing connection.
            mContext.unbindService(mConnection);
            mBound = false;
        }

        Intent intent = new Intent(mContext, MediaPlayerService.class);
        mContext.stopService(intent);

    }

    @Override
    public String getPlayingUrl() {
        if(mService != null && mService.isPlaying()){
            return mService.getURL();
        }
        return null;
    }

    @Override
    public void registerStreamPlayerListener(StreamPlayerListener listener) {
        mStreamPlayerListener = listener;
        if(mService != null){
            mService.addListener(listener);
        }
    }

    @Override
    public void unregisterStreamPlayerListener() {
        if (mService != null) {
            mService.removeListener(mStreamPlayerListener);
        }
        mStreamPlayerListener = null;
    }

    @Override
    public void registerTrackListener(TrackListener listener) {
        mTrackListener = listener;
        if(mService != null) {
            mService.getMetaData(listener);
        }
    }

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
            if(mTrackListener != null){
                mService.getMetaData(mTrackListener);
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
