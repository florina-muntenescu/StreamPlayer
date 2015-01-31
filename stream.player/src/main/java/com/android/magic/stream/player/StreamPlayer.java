package com.android.magic.stream.player;

import android.support.annotation.NonNull;

/**
 * Stream Player that creates a service and a thread and handles the
 * {@link android.media.MediaPlayer}.
 * The library will stop the service when the task of your app is removed.
 * <p/>
 * The StreamPlayer API methods require the following permissions to access the internet and to
 * keep the processor from sleeping:
 * <br>
 * <pre >
 * &lt;uses-permission android:name="android.permission.INTERNET" /&gt;
 * &lt;uses-permission android:name="android.permission.WAKE_LOCK" /&gt;
 * </pre>
 * <p/>
 * The following service and receiver must be addded:
 * <br>
 * <pre>
 * &lt;receiver android:name=".MusicIntentReceiver" &gt;
 * &lt;intent-filter &gt;
 * &lt;action android:name="android.media.AUDIO_BECOMING_NOISY"/&gt;
 * &lt; / intent-filter /&gt;
 * &lt;receiver &gt;
 *
 * &lt;service android:name="com.android.magic.MediaPlayerService /&gt;
 * </pre>
 * <p/>
 * Declare {@link com.android.magic.stream.player.MusicIntentReceiver} if you want to automatically
 * stop the music playing when removing the headphones.
 * Register a {@link StreamPlayerListener} to receive
 * notifications about different {@link android.media.MediaPlayer}'s states.
 * Register a {@link MetadataListener} to receive notifications about
 * track changes.
 */
public interface StreamPlayer {

    /**
     * Plays the stream
     *
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
     *
     * @return the url of the radio playing or null
     */
    public String getPlayingUrl();

    /**
     * Register a listener to be notified about player states
     *
     * @param listener
     */
    public void registerStreamPlayerListener(StreamPlayerListener listener);

    /**
     * Unregister the {@link com.android.magic.stream.player.StreamPlayerListener}
     */
    public void unregisterStreamPlayerListener();

    /**
     * Register a listener to be notified about track changes for the current stream
     */
    public void registerTrackListener(MetadataListener listener);

    /**
     * Unregister the track listener
     */
    public void unregisterTrackListener();


}
