package com.android.magic.stream.player;

/**
 * Listener that reacts on different states of the player service
 */
public interface StreamPlayerListener {

    /**
     * A callback made by a MediaPlayerService onto its listeners to indicate that a player was
     * successfully initialized.
     */
    public void onPlaying(final String url);

    /**
     * A callback made by a MediaPlayerService onto its listeners to indicate that a player
     * encountered an error.
     *
     * @param error - the stream player error
     */
    public void onError(StreamPlayerError error);

    /**
     * A callback made by a MediaPlayerService onto its clients to indicate that a player has
     * stopped.
     */
    public void onPlayerStop();

}