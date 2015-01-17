package com.android.magic.stream.player;

import android.content.Context;

/**
 * Creates or retrieves an instance of the {@link StreamPlayer}
 * Only one instance of {@link com.android.magic.stream.player.StreamPlayer} is allowed
 */
public class StreamPlayerFactory {

    private static StreamPlayerImplementation mStreamPlayerImplementation;

    public static StreamPlayer getStreamPlayerInstance(Context context) {
        if(context == null)
            return null;

        if (mStreamPlayerImplementation == null) {
            mStreamPlayerImplementation = new StreamPlayerImplementation(context
                    .getApplicationContext());
        }
        return mStreamPlayerImplementation;
    }
}
