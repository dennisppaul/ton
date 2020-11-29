package de.hfkbremen.ton;

import static de.hfkbremen.ton.Ton.DEFAULT_ATTACK;
import static de.hfkbremen.ton.Ton.DEFAULT_DECAY;
import static de.hfkbremen.ton.Ton.DEFAULT_RELEASE;
import static de.hfkbremen.ton.Ton.DEFAULT_SAMPLING_RATE;
import static de.hfkbremen.ton.Ton.DEFAULT_SUSTAIN;

//@TODO(investigate some glitches that occur occasionally)
public class ADSR implements DSPNodeOutput {

    /*
     *       [ NODE_ADSR           ]
     *       +---------------------+
     *       |                     |
     * IN00--| SIGNAL       SIGNAL |--OUT00
     *       |                     |
     *       +---------------------+
     *
     *       @description(
     *          the ADSR node provides a envelope with four different stages:
     *          (A)ttack (D)ecay (S)ustain (R)elease. it is usually used to
     *          control the amplitude of an *oscillator*.
     *
     *          |----->|-->|   |-->|
     *          |---A--|-D-|-S-|-R-|
     *          |      /\
     *          |     /  \
     *          |    /    \_____
     *          |   /        ^  \
     *          |  /         |   \
     *          | /          |    \
     *          |/___________|_____\
     *          |
     *          |Press          |Release
     *       )
     *
     *
     */

    private static final boolean DEBUG_ADSR = false;
    private final int mSamplingRate;
    private final float FADE_TO_ZERO_RATE_SEC;
    private float mAttack = DEFAULT_ATTACK;
    private float mDecay = DEFAULT_DECAY;
    private float mSustain = DEFAULT_SUSTAIN;
    private float mRelease = DEFAULT_RELEASE;
    private ENVELOPE_STATE mState;
    private float mAmp = 0.0f;
    private float mDelta = 0.0f;

    public ADSR(int pSamplingRate) {
        mSamplingRate = pSamplingRate;
        FADE_TO_ZERO_RATE_SEC = 0.05f;
        setState(ENVELOPE_STATE.IDLE);
    }

    public ADSR() {
        this(DEFAULT_SAMPLING_RATE);
    }

    @Override
    public float output() {
        step();
        return mAmp;
    }

    public void start() {
        check_scheduled_attack_state();
    }

    public void stop() {
        check_scheduled_release_state();
    }

    public float get_attack() { return mAttack; }

    public void set_attack(float pAttack) { mAttack = pAttack; }

    public float get_decay() { return mDecay; }

    public void set_decay(float pDecay) { mDecay = pDecay; }

    public float get_sustain() { return mSustain; }

    public void set_sustain(float pSustain) { mSustain = pSustain; }

    public float get_release() { return mRelease; }

    public void set_release(float pRelease) { mRelease = pRelease; }

    float compute_delta_fraction(float pDelta, float pDuration) {
        return pDuration > 0 ? (pDelta / mSamplingRate) / pDuration : pDelta;
    }

    private void check_scheduled_attack_state() {
        if (mAmp > 0.0f) {
            if (mState != ENVELOPE_STATE.PRE_ATTACK_FADE_TO_ZERO) {
                mDelta = compute_delta_fraction(-mAmp, FADE_TO_ZERO_RATE_SEC);
                setState(ENVELOPE_STATE.PRE_ATTACK_FADE_TO_ZERO);
            }
        } else {
            mDelta = compute_delta_fraction(1.0f, mAttack);
            setState(ENVELOPE_STATE.ATTACK);
        }
    }

    private void check_scheduled_release_state() {
        if (mState != ENVELOPE_STATE.RELEASE) {
            mDelta = compute_delta_fraction(-mAmp, mRelease);
            setState(ENVELOPE_STATE.RELEASE);
        }
    }

    private void step() {
        switch (mState) {
            case IDLE:
            case SUSTAIN:
                break;
            case ATTACK:
                // increase amp to sustain_level in ATTACK sec
                mAmp += mDelta;
                if (mAmp >= 1.0f) {
                    mAmp = 1.0f;
                    mDelta = compute_delta_fraction(-(1.0f - mSustain), mDecay);
                    setState(ENVELOPE_STATE.DECAY);
                }
                break;
            case DECAY:
                // decrease amp to sustain_level in DECAY sec
                mAmp += mDelta;
                if (mAmp <= mSustain) {
                    mAmp = mSustain;
                    setState(ENVELOPE_STATE.SUSTAIN);
                }
                break;
            case RELEASE:
                // decrease amp to 0.0 in RELEASE sec
                mAmp += mDelta;
                if (mAmp <= 0.0f) {
                    mAmp = 0.0f;
                    setState(ENVELOPE_STATE.IDLE);
                }
                break;
            case PRE_ATTACK_FADE_TO_ZERO:
                mAmp += mDelta;
                if (mAmp <= 0.0f) {
                    mAmp = 0.0f;
                    mDelta = compute_delta_fraction(1.0f, mAttack);
                    setState(ENVELOPE_STATE.ATTACK);
                }
                break;
        }
    }

    private void setState(ENVELOPE_STATE pState) {
        if (DEBUG_ADSR) {
            System.out.print(pState.name());
            System.out.print(" : ");
            System.out.println(mAmp);
        }
        mState = pState;
    }

    private enum ENVELOPE_STATE {
        IDLE, ATTACK, DECAY, SUSTAIN, RELEASE, PRE_ATTACK_FADE_TO_ZERO
    }
}