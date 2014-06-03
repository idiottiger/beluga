package com.idiot2ger.beluga.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Choreographer;
import android.view.Choreographer.FrameCallback;
import android.view.animation.Interpolator;

/**
 * <b>animation core class, diff with system animation, here doesn't have the UI part, only the
 * value, like ValueAnimator, but can work on all android platform</b>
 * <p>
 * usage:<br>
 * 1. create a class extends {@link Animation}, override method
 * {@link #getTransformationValue(float, Object, Object)}
 * <p>
 * 2.create a class implements {@link AnimationCallback}, and register callback
 * {@link #addCallback(AnimationCallback)}
 * 
 * @author r2d2
 * 
 */
public abstract class Animation<T> implements IAnimationModel<T> {

  static final String TAG = "Animation";

  private static final float MAX_FRACTION = 1.0f;

  private static final int MSG_UPDATE_STATE = 1 << 20;

  private List<AnimationCallback<T>> mAnimationCallbackList = new ArrayList<AnimationCallback<T>>();
  private ReentrantLock mLock = new ReentrantLock();
  private State mState = State.STATE_NONE;

  private long mPreviousTime = -1, mCurrentTime = -1;
  private long mDuration = -1;
  private float mFraction = 0.0f;

  private T mStartValue;
  private T mEndValue;

  private AnimationStateHandler mStateHandler = new AnimationStateHandler();
  private AnimationLooper mLooper;

  private Interpolator mInterpolator;

  /**
   * animation state, there are: START, PAUSE, RESUME, STOP, FINISH
   * 
   * @author r2d2
   * 
   */
  public static enum State {
    STATE_NONE, STATE_START, STATE_PAUSE, STATE_RESUME, STATE_STOP, STATE_END, STATE_RESET
  }


  public Animation() {
    this(null, null);
  }

  public Animation(T start, T end) {
    mStartValue = start;
    mEndValue = end;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      mLooper = new AnimationChoreographerLooper();
    } else {
      mLooper = new AnimationHandlerLooper();
    }
  }

  public void setStartValue(T start) {
    mStartValue = start;
  }

  public void setEndValue(T end) {
    mEndValue = end;
  }

  public void start() {
    preCheck();
    if (mState != State.STATE_START) {
      sendAnimationState(State.STATE_START);
    }
  }

  public void pause() {
    preCheck();
    if (mState == State.STATE_START || mState == State.STATE_RESUME) {
      sendAnimationState(State.STATE_PAUSE);
    }
  }


  public void resume() {
    preCheck();
    if (mState == State.STATE_PAUSE) {
      sendAnimationState(State.STATE_RESUME);
    }
  }


  public void stop() {
    preCheck();
    if (mState == State.STATE_START || mState == State.STATE_PAUSE || mState == State.STATE_RESUME) {
      sendAnimationState(State.STATE_STOP);
    }
  }


  public void reset() {
    if (mState != State.STATE_RESET) {
      sendAnimationState(State.STATE_RESET);
    }
  }

  public void setInterpolator(Interpolator interpolator) {
    mInterpolator = interpolator;
  }

  public boolean haveInterpolator() {
    return mInterpolator != null;
  }


  private void preCheck() {
    if (mDuration == -1) {
      throw new IllegalStateException("need setDuration");
    }
    if (mStartValue == null || mEndValue == null) {
      throw new IllegalStateException("need setStartValue and setEndValue");
    }
  }

  private long now() {
    return System.currentTimeMillis();
  }

  public void setDuration(long millisecond) {
    if (millisecond < 1 || millisecond > Long.MAX_VALUE) {
      throw new IllegalArgumentException("duration need between 1 and " + Long.MAX_VALUE);
    }
    mDuration = millisecond;
  }


  public void addCallback(AnimationCallback<T> callback) {
    mLock.lock();
    try {
      mAnimationCallbackList.add(callback);
    } finally {
      mLock.unlock();
    }
  }


  public void removeCallback(AnimationCallback<T> callback) {
    mLock.lock();
    try {
      mAnimationCallbackList.remove(callback);
    } finally {
      mLock.unlock();
    }
  }


  public void removeAllCallback() {
    mLock.lock();
    try {
      mAnimationCallbackList.clear();
    } finally {
      mLock.unlock();
    }
  }


  private void sendAnimationState(State state) {
    Message message = mStateHandler.obtainMessage(MSG_UPDATE_STATE, state.ordinal(), 0);
    message.setTarget(mStateHandler);
    message.sendToTarget();
  }


  private void processMessage(Message msg) {
    final int what = msg.what;
    if (what == MSG_UPDATE_STATE) {
      final State state = State.values()[msg.arg1];

      // if same, skip
      if (mState == state) {
        return;
      }

      updateAnimationState(state);
    }
  }


  private void updateAnimationState(State state) {
    mState = state;

    // state callback
    mLock.lock();
    try {
      for (AnimationCallback<?> callback : mAnimationCallbackList) {
        callback.onAnimationStateChanged(state);
      }
    } finally {
      mLock.unlock();
    }

    if (state == State.STATE_START || state == State.STATE_RESUME) {
      mCurrentTime = mPreviousTime = now();

      // if start, need set percent to zero
      if (state == State.STATE_START) {
        mFraction = 0.0f;
      }
      mLooper.startLoop();
    } else if (state == State.STATE_PAUSE || state == State.STATE_STOP) {
      mLooper.stopLoop();
    } else if (state == State.STATE_RESET) {
      mLooper.stopLoop();
      mFraction = 0.0f;
    }
  }

  private void animationLoop() {
    mCurrentTime = now();
    mFraction += (((float) (mCurrentTime - mPreviousTime)) / mDuration);
    mFraction = Math.min(mFraction, MAX_FRACTION);

    // cal the interpolator
    final float interpolator = mInterpolator == null ? 0 : mInterpolator.getInterpolation(mFraction);
    final float newFraction = mInterpolator == null ? mFraction : interpolator;
    T value = getTransformationValue(newFraction, mStartValue, mEndValue);

    mLock.lock();
    try {
      for (AnimationCallback<T> callback : mAnimationCallbackList) {
        callback.onAnimationing(mFraction, value);
      }
    } finally {
      mLock.unlock();
    }

    mPreviousTime = mCurrentTime;

    if (mFraction >= MAX_FRACTION) {
      sendAnimationState(State.STATE_END);
      mLooper.stopLoop();
    }
  }


  private static interface AnimationLooper {

    public void startLoop();

    public void stopLoop();

  }


  private class AnimationHandlerLooper implements AnimationLooper {

    private Handler mHandler;
    private Runnable mRunnable;
    private volatile boolean mIsRunning;

    AnimationHandlerLooper() {
      mHandler = new Handler();

      mRunnable = new Runnable() {
        @Override
        public void run() {
          if (!mIsRunning) {
            return;
          }
          animationLoop();
          mHandler.post(mRunnable);
        }
      };

      mIsRunning = false;
      Log.w(TAG, "using AnimationHandlerLooper...");
    }

    @Override
    public void startLoop() {
      mIsRunning = true;
      mHandler.removeCallbacks(mRunnable);
      mHandler.post(mRunnable);
    }

    @Override
    public void stopLoop() {
      mIsRunning = false;
      mHandler.removeCallbacks(mRunnable);
    }

  }


  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  private class AnimationChoreographerLooper implements AnimationLooper {

    private Choreographer mChoreographer;
    private FrameCallback mCallback;
    private volatile boolean mIsRunning;

    AnimationChoreographerLooper() {
      mChoreographer = Choreographer.getInstance();

      mCallback = new FrameCallback() {

        @Override
        public void doFrame(long frameTimeNanos) {
          if (!mIsRunning) {
            return;
          }
          animationLoop();
          mChoreographer.postFrameCallback(mCallback);
        }
      };

      mIsRunning = false;

      Log.w(TAG, "using AnimationChoreographerLooper...");
    }

    @Override
    public void startLoop() {
      mIsRunning = true;
      mChoreographer.removeFrameCallback(mCallback);
      mChoreographer.postFrameCallback(mCallback);
    }

    @Override
    public void stopLoop() {
      mIsRunning = false;
      mChoreographer.removeFrameCallback(mCallback);
    }

  }



  /**
   * 
   * @author r2d2
   * 
   */
  @SuppressLint("HandlerLeak")
  private class AnimationStateHandler extends Handler {

    @Override
    public void handleMessage(Message msg) {
      processMessage(msg);
    }
  }
}
