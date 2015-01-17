package com.android.magic.stream.player;

import android.support.annotation.NonNull;

/**
 * Created by android on 1/14/15.
 */
public interface StreamPlayer {

    /**
     * Plays the stream
     * @param url the stream that will be played
     */
    public void play(@NonNull final String url);

    /**
     * Pauses the currently playing stream
     */
    public void pause();

    /**
     * Stops the currently playing stream
     */
    public void stop();

    /**
     * Shuts down completely the player
     */
    public void shutDown();

    /**
     * Retrieves the playing URL or null if there's no url currently playing
     * @return the url of the radio playing or null
     */
    public String getPlayingUrl();

    /**
     * Register a listener to be notified about player states
     * @param listener
     */
    public void registerStreamPlayerListener(StreamPlayerListener listener);

    /**
     * Unregister the {@link com.android.magic.stream.player.StreamPlayerListener}
     */
    public void unregisterStreamPlayerListener();

    /**
     * Register a listener to be notified about track changes for the current stream
     * @param listener
     */
    public void registerTrackListener(TrackListener listener);

    /**
     * Unregister the track listener
     */
    public void unregisterTrackListener();


}
