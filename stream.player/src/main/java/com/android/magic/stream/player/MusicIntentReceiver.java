package com.android.magic.stream.player;

import android.content.Context;
import android.content.Intent;

/**
 * Many well-written applications that play audio automatically stop playback when an event occurs
 * that causes the audio to become noisy (output through external speakers). For instance,
 * this might happen when a user is listening to music through headphones and accidentally
 * disconnects the headphones from the device. However, this behavior does not happen automatically.
 * If you don't implement this feature, audio plays out of the device's external speakers,
 * which might not be what the user wants.
 *
 * Add this receiver in the Manifest file to automatically stop the player and the service when the
 * headphones are removed
 */
public class MusicIntentReceiver extends android.content.BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(
                android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            StreamPlayerFactory.getStreamPlayerInstance(context).stop();
        }
    }
}