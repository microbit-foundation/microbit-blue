package com.bluetooth.mwoolley.microbitbledemo;

import android.media.AudioManager;
import android.media.ToneGenerator;

/**
 * Created by Martin on 10/02/2016.
 */
public class AudioToneMaker {

    private static AudioToneMaker instance;
    private ToneGenerator tone_gen;

    private AudioToneMaker() {
        tone_gen = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
    }

    public static synchronized AudioToneMaker getInstance() {
        if (instance == null) {
            instance = new AudioToneMaker();
        }
        return instance;
    }

    public void playTone(int tone) {
        if (tone_gen.startTone(tone)) {
            try {
                Thread.sleep(100);
                tone_gen.stopTone();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
