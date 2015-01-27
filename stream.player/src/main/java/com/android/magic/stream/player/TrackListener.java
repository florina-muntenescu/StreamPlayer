package com.android.magic.stream.player;

import android.support.annotation.Nullable;

/**
 * Listener that notifies when the currently playing that has been changed
 * The metadata usually contains the artist's name and the track title separated by "-"
 * The metadata data can also be missing, in which case the track will be a null string.
 * The metadata is requested every 15 seconds, but the {@link TrackListener}#onTrackChanged method
 * is only triggered when the retrieved track is a new one.
 * When playing a radio stream, for example, it has been noticed that when a new track is being
 * played, at first, the metadata retrieved is null and only after some seconds, the new playing
 * track data is correct.
 */
public interface TrackListener {

    /**
     * Notifies when a new track is being played and which track it is
     *
     * @param track the currently playing track
     */
    public void onTrackChanged(@Nullable final String track);
}
