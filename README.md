# StreamPlayer

StreamPlayer is a simple audio streaming library that will:

  - play/pause/stop a stream
  - notify when these happen on the StreamPlayerListener
  - if your stream has metadata, it will notify you on MetadataListener about it 
  - handle task removing and automatically stop the player
  - handle headphones removal and automatically stop the player

### Download

Checkout the [downloads] folder for a release jar.

### Installation

Put the jar in your libs folder and add this on your build.gradle file:
```groovy
compile files('libs/StreamPlayer.jar')
```

What to add in the *AndroidManifest.xml* file:

To allow playing, the following service needs to be added:
```xml
<service android:name="com.android.magic.stream.player.MediaPlayerService"/>
```
To allow the StreamPlayer library to handle the playing when the audio becomes nosy (e.g. when the user removes the headphones) add the following receiver
```xml
<receiver android:name="com.android.magic.stream.player.MusicIntentReceiver">
    <intent-filter>
        <action android:name="android.media.AUDIO_BECOMING_NOISY" />
    </intent-filter>
</receiver>
```
To keep the processor from sleeping (so the Android OS doesn't kill your process), add this permission:
```xml
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

### License

    Copyright 2015 Florina Muntenescu
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
       http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


[downloads]:https://github.com/florina-muntenescu/StreamPlayer/tree/master/downloads
