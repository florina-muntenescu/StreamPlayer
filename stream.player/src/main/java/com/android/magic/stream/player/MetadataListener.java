package com.android.magic.stream.player;

import android.support.annotation.Nullable;

/**
 * Listener that notifies when the currently streaming metadata has changed.
 * The metadata usually contains the artist's name and the track title separated by "-"
 * The metadata data can also be missing, in which case the track will be a null string.
 * The metadata is requested every 15 seconds, but the {@link MetadataListener}#onMetadataChanged method
 * is only triggered when the retrieved track is a new one.
 * When playing a radio stream, for example, it has been noticed that when a new track is being
 * played, at first, the metadata retrieved is null and only after some seconds, the new playing
 * track data is correct.
 */
public interface MetadataListener {

    /**
     * Notifies when the metadata of the stream has changed
     *
     * @param metadata the currently playing stream's metadata
     */
    public void onMetadataChanged(@Nullable final String metadata);
}
