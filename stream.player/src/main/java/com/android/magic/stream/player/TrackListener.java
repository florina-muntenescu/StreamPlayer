package com.android.magic.stream.player;

/**
 * Listener that notifies when the currently playing that has been changed
 */
public interface TrackListener {

    /**
     * Notifies when a new track is being played and which track it is
     * @param track the currently playing track
     */
    public void onTrackChanged(final String track);
}
