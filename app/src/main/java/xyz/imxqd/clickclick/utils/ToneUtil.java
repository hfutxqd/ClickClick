package xyz.imxqd.clickclick.utils;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.util.List;

public class ToneUtil {
    private static final int sampleRate = 8000;
    public static class Tone {
        public int freq; // Hz
        public int duration; // ms
    }

    public static int getTotalDuration(List<Tone> tones) {
        int totalDur = 0;
        for (Tone t : tones) {
            totalDur += t.duration;
        }
        return totalDur;
    }

    public static AudioTrack genAudio(List<Tone> tones) {
        int totalDur = getTotalDuration(tones);
        int numSamples = sampleRate / 1000 * totalDur;
        double sample[] = new double[numSamples];
        int i = 0;
        for (Tone t : tones) {
            int numToneSamples = sampleRate / 1000 * t.duration;
            int toneStart = i;
            for (; i < toneStart + numToneSamples; ++i) {
                sample[i] = Math.sin(2 * Math.PI * i / (sampleRate / t.freq));
            }
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        byte generatedSnd[] = new byte[2 * numSamples];
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }
        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
                AudioTrack.MODE_STATIC);
        audioTrack.write(generatedSnd, 0, generatedSnd.length);
        return audioTrack;

    }
}
