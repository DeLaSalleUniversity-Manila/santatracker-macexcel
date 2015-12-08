/*
 * Copyright (C) 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.apps.santatracker.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.util.SparseArray;

/**
 * Handle audio playback, including repeats, stopping and muting of multiple streams.
 */
public class AudioPlayer {

    private Context mContext;

    private SparseArray<MediaPlayer> mStreams;

    private boolean mMuted = false;

    /**
     * Volume for all media playback.
     */
    private static float VOLUME_MULTIPLIER = 0.25f;

    public AudioPlayer(Context context) {
        mContext = context;
        mStreams = new SparseArray<>();
        this.mMuted = false;
    }

    /**
     * Play a sound identified by a resource ID.
     *
     * @param resId Resource ID of sound to play
     * @param loop  If set, the track is repeated indefinitely.
     */
    public void playTrack(final int resId, final boolean loop) {
        MediaPlayer mediaPlayer = MediaPlayer.create(mContext, resId);
        mStreams.put(resId, mediaPlayer);
        mediaPlayer.setLooping(loop);
        mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                startMedia(mp);
            }
        });
        mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                if (!mp.isLooping()) {
                    mStreams.remove(resId);
                }
            }
        });
    }

    /**
     * Mute all audio streams and only play this sound.
     *
     * @see #playTrack(int, boolean)
     */
    public void playTrackExclusive(final int resId, final boolean loop) {
        boolean restart = false;
        MediaPlayer mp = mStreams.get(resId);
        try {
            if (mp == null || !mp.isPlaying()) {
                restart = true;
            }
        } catch (IllegalStateException e) {
            // Media player was not initialised or was released
            restart = true;
        }

        if (restart) {
            stopAll();
            playTrack(resId, loop);
        }
    }

    /**
     * Initialise the volume and start the MediaPlayer.
     */
    private void startMedia(MediaPlayer mp) {
        if (mMuted) {
            mp.setVolume(0f, 0f);
        } else {
            mp.setVolume(VOLUME_MULTIPLIER, VOLUME_MULTIPLIER);
        }
        mp.start();
    }

    public void stop(int resId) {
        MediaPlayer mp = mStreams.get(resId);
        if (mp != null) {
            mp.stop();
            mp.release();
            mStreams.remove(resId);
        }
    }

    public void muteAll() {
        mMuted = true;
        for (int i = 0; i < mStreams.size(); i++) {
            mStreams.valueAt(i).setVolume(0f, 0f);
        }
    }

    public void unMuteAll() {
        mMuted = false;
        for (int i = 0; i < mStreams.size(); i++) {
            mStreams.valueAt(i).setVolume(VOLUME_MULTIPLIER, VOLUME_MULTIPLIER);
        }
    }

    public void pauseAll() {
        for (int i = 0; i < mStreams.size(); i++) {
            mStreams.valueAt(i).pause();
        }
    }

    public void resumeAll() {
        for (int i = 0; i < mStreams.size(); i++) {
            startMedia(mStreams.valueAt(i));
        }
    }

    public void stopAll() {
        // Stop all audio
        for (int i = 0; i < mStreams.size(); i++) {
            MediaPlayer mp = mStreams.valueAt(i);
            mp.release();
            mStreams.removeAt(i);
        }
    }
}
