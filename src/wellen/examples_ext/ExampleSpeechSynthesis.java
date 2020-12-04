package wellen.examples_ext;

import processing.core.PApplet;
import wellen.Beat;
import wellen.SpeechSynthesis;

/**
 * this example demonstrates how to use the built-in speech synthesis engine ( MacOS only )
 */
public class ExampleSpeechSynthesis extends PApplet {

    private SpeechSynthesis mSpeech;
    private String[] mWords;
    private int mBeatCount;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        String mText = "I know not by what power I am made bold, Nor how it may concern my modesty, In such a " +
                "presence here to plead" +
                " my thoughts; But I beseech your grace that I may know The worst that may " + "befall me in " +
                "this case, If I refuse to " +
                "wed Demetrius.";
        mWords = split(mText, ' ');
        printArray(SpeechSynthesis.list());
        mSpeech = new SpeechSynthesis();
        mSpeech.blocking(false);

        Beat mBeat = new Beat(this, 140);
    }

    public void draw() {
        background(mBeatCount * 10 % 255);
    }

    public void beat(int pBeatCount) {
        mBeatCount = pBeatCount;
        int mWordIndex = pBeatCount % mWords.length;
        mSpeech.say("Alex", mWords[mWordIndex]);
    }

    public static void main(String[] args) {
        PApplet.main(ExampleSpeechSynthesis.class.getName());
    }
}