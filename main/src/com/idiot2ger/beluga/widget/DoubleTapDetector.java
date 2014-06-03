package com.idiot2ger.beluga.widget;

import android.view.MotionEvent;
import android.view.ViewConfiguration;

public class DoubleTapDetector {

  static final String TAG = "DoubleTapDetector";

  private static final int DOUBLE_TAP_TIMEOUT = ViewConfiguration.getDoubleTapTimeout();
  private static final int MOVE_MIN_DISTANCE = 16;

  private static enum EventState {
    NONE, START, END, CANCEL
  }

  private DoubleTapListener mDoubleTapListener;
  private float mPx, mPy;
  private long mFirstDownTime, mSecondDownTime;
  private EventState mState;

  public DoubleTapDetector() {
    reset();
  }



  public void setDoubleTapListener(DoubleTapListener listener) {
    mDoubleTapListener = listener;
  }

  public boolean onTouchEvent(MotionEvent event) {
    final int action = event.getAction() & MotionEvent.ACTION_MASK;
    boolean result = true;
    switch (action) {
      case MotionEvent.ACTION_DOWN:
        mPx = event.getX();
        mPy = event.getY();
        if (mFirstDownTime == -1) {
          mSecondDownTime = -1;
          mFirstDownTime = event.getEventTime();
        } else if (mSecondDownTime == -1) {
          if (isInDoubleTapTimeLimit(event.getEventTime())) {
            mSecondDownTime = event.getEventTime();
          } else {
            notifyEventChange(EventState.CANCEL);
            result = false;
          }
        }
        break;
      case MotionEvent.ACTION_MOVE:
        if (isMoving(mPx, mPy, event.getX(), event.getY())) {
          notifyEventChange(EventState.CANCEL);
          result = false;
        }
        break;
      case MotionEvent.ACTION_UP:
        if (mSecondDownTime == -1) {
          if (mFirstDownTime == -1 || !isInDoubleTapTimeLimit(event.getEventTime())) {
            result = false;
          } else {
            notifyEventChange(EventState.START);
          }
        } else if (isInDoubleTapTimeLimit(event.getEventTime())) {
          if (mDoubleTapListener != null) {
            mDoubleTapListener.onDoubleTap(event);
          }
          notifyEventChange(EventState.END);
          // all done, will reset
          reset();
        } else {
          result = false;
        }
        break;
    }

    if (!result) {
      reset();
    }

    return result;
  }

  private boolean isInDoubleTapTimeLimit(long timeEnd) {
    return (timeEnd - mFirstDownTime) <= DOUBLE_TAP_TIMEOUT;
  }

  private void notifyEventChange(EventState state) {
    if (mDoubleTapListener != null && mState != state) {
      if (state == EventState.START) {
        mDoubleTapListener.onDoubleTapBegin();
      } else {
        mDoubleTapListener.onDoubleTapEnd();
      }
    }
    mState = state;
  }

  private boolean isMoving(float x1, float y1, float x2, float y2) {
    return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)) >= MOVE_MIN_DISTANCE;
  }

  private void reset() {
    mFirstDownTime = mSecondDownTime = -1;
    mState = EventState.NONE;
  }



  public static interface DoubleTapListener {

    public void onDoubleTapBegin();

    public void onDoubleTapEnd();

    public void onDoubleTap(MotionEvent event);
  }
}
