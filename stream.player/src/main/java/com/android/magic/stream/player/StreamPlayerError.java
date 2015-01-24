package com.android.magic.stream.player;

/**
 * Errors returned by the {@link com.android.magic.stream.player.StreamPlayer}
 */
public enum StreamPlayerError {
    /**
     * Player timed out or operation took longer to complete
     */
    PLAYER_TIMED_OUT,

    /**
     * File or network error
     */
    PLAYER_IO_ERROR,

    /**
     * Media framework does not support the stream or coding standard
     */
    PLAYER_UNSUPPORTED,

    /**
     * Unknown player error
     */
    PLAYER_UNKNOWN_ERROR
}
