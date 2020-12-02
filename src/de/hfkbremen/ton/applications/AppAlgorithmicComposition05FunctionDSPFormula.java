package de.hfkbremen.ton.applications;

import de.hfkbremen.ton.DSP;
import de.hfkbremen.ton.Ton;
import processing.core.PApplet;

public class AppAlgorithmicComposition05FunctionDSPFormula extends PApplet {

    private final float mFreq = 220.0f;
    private AudioFormula mFormula = new MAudioFormulaAwayAndAway();
    private int mCounter = 0;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Ton.dumpAudioInputAndOutputDevices();
        DSP.start(this);
    }

    public void draw() {
        background(255);
        stroke(0);
        final int mBufferSize = DSP.buffer_size();
        if (DSP.buffer() != null) {
            for (int i = 0; i < mBufferSize; i++) {
                final float x = map(i, 0, mBufferSize, 0, width);
                point(x, map(DSP.buffer()[i], -1, 1, 0, height));
            }
        }
    }

    public void keyPressed() {
        switch (key) {
            case '1':
                mFormula = new AudioFormulaKnisterKnister();
                break;
            case '2':
                mFormula = new MAudioFormulaAwayAndAway();
                break;
        }
    }

    public void audioblock(float[] pOutputSamples) {
        for (int i = 0; i < pOutputSamples.length; i++) {
            pOutputSamples[i] = mFormula.render(mCounter++);
        }
    }

    interface AudioFormula {
        float render(int pCounter);
    }

    class AudioFormulaKnisterKnister implements AudioFormula {
        public float render(int pCounter) {
            final float mSeconds = (float) pCounter / DSP.sample_rate();
            float v;
            v = abs(sin(mSeconds * cos(mSeconds * 1.1f))) * 0.1f;
            v = (mFreq + (v % 0.037f));
            v = sin(mSeconds * v + PI * 0.33f);
            v *= v;
            v *= 2 + sin(mSeconds * 21.9f);
            v -= 1;
            v = DSP.clamp(v, -1.0f, 1.0f);
            v *= 0.75f;
            return v;
        }
    }

    class MAudioFormulaAwayAndAway implements AudioFormula {
        public float render(int pCounter) {
            final float mSeconds = (float) pCounter / DSP.sample_rate();
            final float mSecondsRad = 2.0f * PI * mSeconds;
            float v;
            v = sin(mSecondsRad * (mFreq + sin(mSecondsRad * 0.001f) * 110.0f));
            v -= sin(mSecondsRad * mFreq * 13) * 0.1f;
            v *= 1.0 + abs(sin(mSecondsRad * 0.47f)) * 2.0f;
            v = DSP.flip(v);
            v *= pow(sin(mSecondsRad * 0.01f), 8) * 0.9f + 0.1f;
            v = DSP.clamp(v, -1.0f, 1.0f);
            v *= 0.75f;
            return v;
        }
    }

    public static void main(String[] args) {
        PApplet.main(AppAlgorithmicComposition05FunctionDSPFormula.class.getName());
    }
}