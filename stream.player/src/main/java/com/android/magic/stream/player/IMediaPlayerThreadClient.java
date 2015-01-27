package com.android.magic.stream.player;

/**
 * Listener of the thread that handles the {@link android.media.MediaPlayer}
 */
/*default*/ interface IMediaPlayerThreadClient {

    /**
     * A callback made by a MediaPlayerThread onto its clients to indicate that a player is
     * initializing.
     */
    public void onInitializePlayerStart();

    /**
     * A callback made by a MediaPlayerThread onto its clients to indicate that a player was
     * successfully initialized.
     */
    public void onPlaying();

    /**
     * A callback made by a MediaPlayerThread onto its clients to indicate that a player has
     * stopped.
     */
    public void onStop();

    /**
     * A callback made by a MediaPlayerThread onto its clients to indicate that a player
     * encountered an error.
     *
     * @param what  - the what returned by {@link android.media.MediaPlayer}#onError
     * @param extra - the extra returned by {@link android.media.MediaPlayer}#onError
     */
    public void onError(int what, int extra);
}