package com.android.magic.stream.player;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * An extension of android.app.Service class which provides management to a MediaPlayerThread.</p>
 * Add this service in the AndroidManifest file.
 */
public class MediaPlayerService extends Service implements IMediaPlayerThreadClient {

    private static final String LOG_TAG = MediaPlayerService.class.getSimpleName();

    public static final String MEDIA_PLAYER_SERVICE = "stream.player.MediaPlayerService";

    private MediaPlayerThread mMediaPlayerThread;
    private final Binder mBinder = new MediaPlayerBinder();
    private String mURL;
    private String mCurrentTrack;
    private Subscription mTrackMetadataSubscription;

    private StreamPlayerListener mListener;
    private TrackListener mTrackListener;

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
     * @return
     */
    public MediaPlayer getMediaPlayer() {
        return mMediaPlayerThread.getMediaPlayer();
    }


    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }


    /**
     * Add a listener of this service.
     *
     * @param listener The listener of this service, which implements the  {@link
     *                 StreamPlayerListener}  interface
     */
    public void addListener(StreamPlayerListener listener) {
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
     *                 com.android.magic.stream.player.TrackListener}  interface
     */
    public void addTrackListener(TrackListener listener) {
        mTrackListener = listener;
    }

    /**
     * Removes the listener for the track change
     */
    public void removeTrackListener() {
        mTrackListener = null;
        if (mTrackMetadataSubscription != null) {
            mTrackMetadataSubscription.unsubscribe();
        }
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
    public void onError() {
        if (mListener != null) {
            mListener.onError();
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
        if(mTrackListener != null){
            getMetaData(mTrackListener);
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

    private void getMetaData(final TrackListener trackListener) {
        Log.d(LOG_TAG, "get metadata");
        final Long timeBefore = System.currentTimeMillis();
        URL url = null;
        try {
            url = new URL(mURL);
        } catch (MalformedURLException e) {

        }
        MetaDataRetriever retriever = new MetaDataRetriever(url);
        Observable<String> metadataObservable = retriever.getMetadataAsync();
        mTrackMetadataSubscription = metadataObservable
                .subscribeOn(
                        Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(
                new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        // nothing
                        Log.d(LOG_TAG, "track metadata completed");
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(String metadata) {

                        final Long timeElapsed = System.currentTimeMillis() - timeBefore;
                        final String timeGetMetadata = String.format(
                                "%1$,.5f", (double) timeElapsed / 1000);
                        Log.d(LOG_TAG, "time metadata " + timeGetMetadata + " " + metadata);
                        Log.d(LOG_TAG, "metadata: " + metadata);

                        if (isPlaying() && !metadata.equals(mCurrentTrack)) {
                            mCurrentTrack = metadata;
                            trackListener.onTrackChanged(mCurrentTrack);
                        }
                    }
                });
    }
}