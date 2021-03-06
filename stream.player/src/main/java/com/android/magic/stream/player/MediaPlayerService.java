package com.android.magic.stream.player;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

/**
 * An extension of android.app.Service class which provides management to a MediaPlayerThread.
 * <p/>
 * Add this service in the AndroidManifest file.
 */
public class MediaPlayerService extends Service implements IMediaPlayerThreadClient {

    private static final String LOG_TAG = MediaPlayerService.class.getSimpleName();

    public static final String MEDIA_PLAYER_SERVICE = "stream.player.MediaPlayerService";

    private static final int METADATA_REQUEST_TIME_INTERVAL_SECONDS = 15; // seconds

    private MediaPlayerThread mMediaPlayerThread;
    private final Binder mBinder = new MediaPlayerBinder();
    private String mURL;
    private String mCurrentTrack;
    private Subscription mTrackMetadataSubscription;

    private StreamPlayerListener mListener;
    private MetadataListener mMetadataListener;

    private MusicIntentReceiver mReceiver = new MusicIntentReceiver();

    @Override
    public void onCreate() {
        mMediaPlayerThread = new MediaPlayerThread(this, this);
        mMediaPlayerThread.start();

        IntentFilter intentFilter = new IntentFilter();
        registerReceiver(mReceiver, intentFilter);
    }

    /**
     * A class for clients binding to this service. The client will be passed an object of this
     * class
     * via its onServiceConnected(ComponentName, IBinder) callback.
     */
    public class MediaPlayerBinder extends Binder {
        /**
         * Returns the instance of this service for a client to make method calls on it.
         *
         * @return the instance of this service.
         */
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }

    }

    /**
     * Returns the contained StatefulMediaPlayer
     *
     * @return the {@link android.media.MediaPlayer} or null
     */
    public
    @Nullable
    MediaPlayer getMediaPlayer() {
        if (mMediaPlayerThread != null) {
            return mMediaPlayerThread.getMediaPlayer();
        }
        return null;
    }


    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }


    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        return START_NOT_STICKY;
    }


    /**
     * Add a listener of this service.
     *
     * @param listener The listener of this service, which implements the  {@link
     *                 StreamPlayerListener}  interface
     */
    public void addListener(final StreamPlayerListener listener) {
        mListener = listener;
    }

    /**
     * Removes a listener of this service
     */
    public void removeListener() {
        mListener = null;
    }

    /**
     * Add a listener of the track change.
     *
     * @param listener The listener for the track, which implements the  {@link
     *                 MetadataListener}  interface
     */
    public void addTrackListener(final MetadataListener listener) {
        Log.d(LOG_TAG, "addTrackListener");
        mMetadataListener = listener;
    }

    /**
     * Removes the listener for the track change
     */
    public void removeTrackListener() {
        Log.d(LOG_TAG, "removeTrackListener");
        mMetadataListener = null;
        if (mTrackMetadataSubscription != null) {
            mTrackMetadataSubscription.unsubscribe();
        }
        mCurrentTrack = null;
    }


    public void initializePlayer(final String url) {
        mURL = url;
        mMediaPlayerThread.initializePlayer(mURL);
    }

    public void startMediaPlayer() {
        mMediaPlayerThread.startMediaPlayer();
    }

    public boolean isPlaying() {
        MediaPlayer player = mMediaPlayerThread.getMediaPlayer();
        return player != null && player.isPlaying();
    }

    public boolean isStopped() {
        return mMediaPlayerThread.getMediaPlayer() == null;
    }

    /**
     * Pauses playback
     */
    public void pauseMediaPlayer() {
        Log.d("MediaPlayerService", "pauseMediaPlayer() called");
        mMediaPlayerThread.pauseMediaPlayer();
        stopForeground(true);
        if (mTrackMetadataSubscription != null) {
            mTrackMetadataSubscription.unsubscribe();
        }
        mCurrentTrack = null;
    }

    /**
     * Stops playback
     */
    public void stopMediaPlayer() {
        stopForeground(true);
        mMediaPlayerThread.stopMediaPlayer();
        if (mTrackMetadataSubscription != null) {
            mTrackMetadataSubscription.unsubscribe();
        }
        mCurrentTrack = null;
    }

    public void resetMediaPlayer() {
        stopForeground(true);
        mMediaPlayerThread.resetMediaPlayer();
    }


    @Override
    public void onError(int what, int extra) {
        if (mListener != null) {
            if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
                mListener.onError(StreamPlayerError.PLAYER_UNKNOWN_ERROR);
                return;
            }

            switch (extra) {
                case MediaPlayer.MEDIA_ERROR_IO:
                    mListener.onError(StreamPlayerError.PLAYER_IO_ERROR);
                    break;

                case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                    mListener.onError(StreamPlayerError.PLAYER_TIMED_OUT);
                    break;

                case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                case MediaPlayer.MEDIA_ERROR_MALFORMED:
                    mListener.onError(StreamPlayerError.PLAYER_UNSUPPORTED);
                    break;
                default:
                    mListener.onError(StreamPlayerError.PLAYER_UNKNOWN_ERROR);
                    break;
            }
        }
    }

    @Override
    public void onInitializePlayerStart() {
        startMediaPlayer();
    }

    @Override
    public void onPlaying() {
        if (mListener != null) {
            mListener.onPlaying(mURL);
        }
        stopRetrievingMetadata();
        if (mMetadataListener != null) {
            getMetaData(mMetadataListener);
        }
    }

    @Override
    public void onStop() {
        if (mListener != null) {
            mListener.onPlayerStop();
        }
        if (mTrackMetadataSubscription != null) {
            mTrackMetadataSubscription.unsubscribe();
        }
        mCurrentTrack = null;
    }

    public void unRegister() {
        this.mListener = null;
    }

    public String getURL() {
        return mURL;
    }

    /**
     * This is called if the service is currently running and the user has removed a
     * task that comes from the service's application.
     * Stop the service when the task is removed
     *
     * @param rootIntent
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(MediaPlayerService.class.getSimpleName(), "on task removed");
        stopMediaPlayer();
        this.unRegister();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void stopRetrievingMetadata() {
        if (mTrackMetadataSubscription != null) {
            mTrackMetadataSubscription.unsubscribe();
        }
        mCurrentTrack = null;
    }

    /**
     * Request the metadata every
     * {@link MediaPlayerService}#METADATA_REQUEST_TIME_INTERVAL_SECONDS = 15 and notify the track
     * listener when the track has been changed
     *
     * @param metadataListener listener that listens to track changed
     */
    private void getMetaData(final MetadataListener metadataListener) {
        Log.d(LOG_TAG, "requesting medatada");
        URL url = null;
        try {
            url = new URL(mURL);
        } catch (MalformedURLException e) {

        }
        final MetaDataRetriever retriever = new MetaDataRetriever(url);

        mTrackMetadataSubscription = Schedulers.newThread().createWorker();
        ((Scheduler.Worker) mTrackMetadataSubscription).schedule(
                new Action0() {

                    @Override
                    public void call() {
                        try {
                            final String metadata = retriever.getMetadata();
                            Log.d(LOG_TAG, "metadata retrieved: " + metadata);
                            Log.d(LOG_TAG, "is playing " + isPlaying() + " current track " + mCurrentTrack);
                            if (isPlaying() && !metadata.equals(mCurrentTrack)) {
                                mCurrentTrack = metadata;
                                metadataListener.onMetadataChanged(mCurrentTrack);
                            }

                        } catch (IOException exception) {

                        } finally {
                            // recurse until unsubscribed (schedule will do nothing if unsubscribed)
                            ((Scheduler.Worker) mTrackMetadataSubscription).schedule(
                                    this, METADATA_REQUEST_TIME_INTERVAL_SECONDS, TimeUnit.SECONDS);
                        }
                    }

                });
    }
}