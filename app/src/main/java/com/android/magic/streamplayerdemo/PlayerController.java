package com.android.magic.streamplayerdemo;

import android.content.Context;
import android.util.Log;

import com.android.magic.stream.player.StreamPlayer;
import com.android.magic.stream.player.StreamPlayerFactory;
import com.android.magic.stream.player.StreamPlayerListener;
import com.android.magic.stream.player.TrackListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the player
 */
public class PlayerController implements StreamPlayerListener, TrackListener {

    private static final String LOG_TAG = PlayerController.class.getSimpleName();

    public interface PlayerListener {
        void onPlay(String radioURL);

        void onPlayerStop();

        void onTrackChanged(String track);
    }

    private StreamPlayer mStreamPlayer;


    private List<PlayerListener> mPlayerListenerList;

    private static PlayerController mInstance;

    public static PlayerController getInstance(final Context context){
        if(mInstance == null){
            mInstance = new PlayerController(context);
        }
        return mInstance;
    }

    private PlayerController(Context context) {
        mStreamPlayer = StreamPlayerFactory.getStreamPlayerInstance(context);
        mPlayerListenerList = new ArrayList<PlayerListener>();
    }

    public void registerListener(PlayerListener listener) {
        if (mPlayerListenerList.isEmpty()) {
            mStreamPlayer.registerStreamPlayerListener(this);
            mStreamPlayer.registerTrackListener(this);
        }
        mPlayerListenerList.add(listener);
    }

    public void unregisterListener(PlayerListener listener) {
        mPlayerListenerList.remove(listener);
        if (mPlayerListenerList.isEmpty()) {
            mStreamPlayer.unregisterStreamPlayerListener();
            mStreamPlayer.unregisterTrackListener();
        }
    }

    public String getPlayingUrl() {
        return mStreamPlayer.getPlayingUrl();
    }

    public void play(String url) {
        mStreamPlayer.play(url);
    }

    public void pause() {
        mStreamPlayer.pause();
    }

    public void stop(){
        mStreamPlayer.stop();
    }

    @Override
    public void onInitializePlayerStart(String url) {

    }

    @Override
    public void onPlaying(String url) {
        for (PlayerListener listener : mPlayerListenerList) {
            listener.onPlay(url);
        }
    }

    @Override
    public void onError() {
    }

    @Override
    public void onPlayerStop() {
        for (PlayerListener listener : mPlayerListenerList) {
            listener.onPlayerStop();
        }
    }

    @Override
    public void onTrackChanged(String track) {
        Log.d(LOG_TAG, "track changed " + track + " listeners " + mPlayerListenerList.size());
        for (PlayerListener listener : mPlayerListenerList) {
            listener.onTrackChanged(track);
        }
    }
}
