# StreamPlayer
Simple library for playing audio streams.
The library creates a Service and a Thread and handles the MediaPlayer.
The library will stop the service when the task of your app is removed.
Declare the MusicIntentReceiver in your AndroidManifest.xml if you want to automatically stop the music playing when removing the headphones.
Register a {@link StreamPlayerListener} to receive notifications about different {@link android.media.MediaPlayer}'s states. Register a {@link com.android.magic.stream.player.TrackListener} to receive notifications about track changes.
