package com.idiot2ger.beluga.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;

import com.idiot2ger.beluga.animation.Animation;
import com.idiot2ger.beluga.animation.AnimationCallback;
import com.idiot2ger.beluga.animation.IAnimation.State;
import com.idiot2ger.beluga.widget.DoubleTapDetector.DoubleTapListener;

public class BitmapView extends View implements OnScaleGestureListener, DoubleTapListener {

  static final String TAG = "BitmapView";

  private static boolean DEBUG = false;

  static final float DEFAULT_SCALE_LIMIT = 0.3f;
  static final float DEFAULT_MAX_SCALE = 3.0f;
  static final float DEFAULT_MIN_SCALE = 0.75f;
  static final int ADJUST_ANIMATION_TIME = 240;

  private static enum Mode {
    NONE, MOVE, ZOOM, ANIMATION
  }

  private boolean mIsNeedInit;
  private Bitmap mBitmap;
  private Matrix mDrawMatrix;

  private float mScaleFactor, mMaxScaleFactor, mMinScaleFactor, mDefaultScaleFactor;

  private float mBitmapWidth, mBitmapHeight;
  private float mCanvasWidth, mCanvasHeight;

  private Paint mDrawPaint;
  private ScaleGestureDetector mScaleDetector;
  private boolean mIsScale, mIsDoubleTaping;
  private Mode mMode;

  private float[] mPos = new float[9];
  private AdjustAnimation mAnimation;
  private PointF mMovePoint;
  private DoubleTapDetector mDoubleTapDetector;


  public BitmapView(Context context) {
    this(context, null);
  }

  public BitmapView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public BitmapView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    mDrawMatrix = new Matrix();

    mDrawPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    mMovePoint = new PointF();

    mScaleDetector = new ScaleGestureDetector(context, this);
    mDoubleTapDetector = new DoubleTapDetector();
    mDoubleTapDetector.setDoubleTapListener(this);


    mAnimation = new AdjustAnimation();
    mAnimation.setDuration(ADJUST_ANIMATION_TIME);


    mAnimation.addCallback(new AnimationCallback<Float5Value>() {
      @Override
      public void onAnimationStateChanged(State state) {
        if (state == State.STATE_START) {
          mMode = Mode.ANIMATION;
        } else if (state == State.STATE_END) {
          mMode = Mode.NONE;
        }
      }

      @Override
      public void onAnimationing(float fraction, Float5Value value) {
        mDrawMatrix.postScale(value.scale, value.scale, value.centerX, value.centerY);
        mDrawMatrix.postTranslate(value.x, value.y);
        invalidate();
      }
    });
  }

  public void setBitmapPath(String path) {

  }

  public void setBitmap(Bitmap bitmap) {
    mBitmap = bitmap;
    mIsNeedInit = true;
    postInvalidate();
  }

  public void setBitmapThumbnail(Bitmap thumbnail) {

  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (isBitmapOK() && isCanvasOK(canvas)) {
      if (mIsNeedInit) {
        initDraw(canvas);
      }
      drawBitmap(canvas);
    }
  }



  @Override
  public boolean onTouchEvent(MotionEvent event) {

    if (mMode == Mode.ANIMATION) {
      return false;
    }


    mScaleDetector.onTouchEvent(event);

    final float x = event.getX();
    final float y = event.getY();
    if (isInBitmapArea(x, y)) {
      mDoubleTapDetector.onTouchEvent(event);
    }

    if (!mIsScale && !mIsDoubleTaping) {
      final int action = event.getAction() & MotionEvent.ACTION_MASK;
      switch (action) {
        case MotionEvent.ACTION_DOWN:
          if (isInBitmapArea(x, y)) {
            mMovePoint.set(x, y);
            mMode = Mode.MOVE;
          }
          break;
        case MotionEvent.ACTION_UP:
          if (mMode == Mode.MOVE || mMode == Mode.ZOOM) {
            adjustPosition();
            mMode = Mode.NONE;
          }
          break;
        case MotionEvent.ACTION_MOVE:
          if (mMode == Mode.MOVE) {
            float mx = x - mMovePoint.x;
            float my = y - mMovePoint.y;
            if (mx != 0 || my != 0) {
              move(mx, my);
            }
            mMovePoint.set(x, y);
          }
          break;
      }
    }

    return true;
  }

  private boolean isInBitmapArea(float x, float y) {
    mDrawMatrix.getValues(mPos);
    float minX = mPos[2];
    float minY = mPos[5];

    float maxX = minX + mScaleFactor * mBitmapWidth;
    float maxY = minY + mScaleFactor * mBitmapHeight;

    return x >= minX && x <= maxX && y >= minY && y <= maxY;
  }


  private void adjustPosition() {
    final float startScale = mScaleFactor;
    float endScale = startScale;
    if (startScale > mMaxScaleFactor) {
      endScale = mMaxScaleFactor;
    } else if (startScale < mDefaultScaleFactor) {
      endScale = mDefaultScaleFactor;
    } else if (startScale < mMinScaleFactor) {
      endScale = mMinScaleFactor;
    }
    adjustPosition2(startScale, endScale);
  }

  private void adjustPosition2(float startScale, float endScale) {
    mDrawMatrix.getValues(mPos);

    float startX = mPos[2];
    float startY = mPos[5];


    float sX = startScale * mBitmapWidth;
    float sY = startScale * mBitmapHeight;

    float endX = (mCanvasWidth - sX) / 2.0f;
    float endY = (mCanvasHeight - sY) / 2.0f;


    if (endScale != mDefaultScaleFactor) {
      if (sX > mCanvasWidth) {
        if (startX > 0) {
          endX = 0;
        } else if (startX + sX < mCanvasWidth) {
          endX = mCanvasWidth - sX;
        } else {
          endX = startX;
        }
      }
      if (sY > mCanvasHeight) {
        if (startY > 0) {
          endY = 0;
        } else if (startY + sY < mCanvasHeight) {
          endY = mCanvasHeight - sY;
        } else {
          endY = startY;
        }
      }
    }

    // here set
    mScaleFactor = endScale;

    mAnimation.setAnimation(startScale, startX, startY, endScale, endX, endY);
    mAnimation.start();

    mMode = Mode.ANIMATION;
  }

  private void move(float mx, float my) {
    mDrawMatrix.postTranslate(mx, mScaleFactor == mDefaultScaleFactor ? 0 : my);
    invalidate();
  }

  private boolean isBitmapOK() {
    return mBitmap != null && !mBitmap.isRecycled();
  }

  private boolean isCanvasOK(Canvas canvas) {
    return canvas.getWidth() > 0 && canvas.getHeight() > 0;
  }

  private void initDraw(Canvas canvas) {
    mIsNeedInit = false;
    mDrawMatrix.reset();

    final float bw = mBitmap.getWidth();
    final float bh = mBitmap.getHeight();

    final float cw = canvas.getWidth();
    final float ch = canvas.getHeight();


    mDefaultScaleFactor = Math.min(cw / bw, ch / bh);
    // here set the scales
    if (bw < cw && bh < ch) {
      mMinScaleFactor = 1.0f;
    } else {
      mMinScaleFactor = mDefaultScaleFactor * DEFAULT_MIN_SCALE;
    }
    mMaxScaleFactor = mDefaultScaleFactor * DEFAULT_MAX_SCALE;

    mScaleFactor = mDefaultScaleFactor;
    mBitmapWidth = bw;
    mBitmapHeight = bh;
    mCanvasWidth = cw;
    mCanvasHeight = ch;

    // init the bitmap position
    float x = (mCanvasWidth - mScaleFactor * mBitmapWidth) / 2.0f;
    float y = (mCanvasHeight - mScaleFactor * mBitmapHeight) / 2.0f;
    mDrawMatrix.setScale(mScaleFactor, mScaleFactor);
    mDrawMatrix.postTranslate(x, y);
  }

  private void drawBitmap(Canvas canvas) {
    canvas.drawBitmap(mBitmap, mDrawMatrix, mDrawPaint);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    mIsNeedInit = true;
  }

  @Override
  public boolean onScale(ScaleGestureDetector detector) {

    mIsScale = true;

    float scale = detector.getScaleFactor();
    float newScale = 0;
    if (Float.isInfinite(scale) || Float.isNaN(scale)) {
      return true;
    }

    newScale = mScaleFactor * scale;
    if (newScale >= (mMaxScaleFactor + DEFAULT_SCALE_LIMIT) || newScale <= (mMinScaleFactor - DEFAULT_SCALE_LIMIT)) {
      return true;
    }
    mScaleFactor = newScale;
    mDrawMatrix.postScale(scale, scale, detector.getFocusX(), detector.getFocusY());
    invalidate();

    return true;
  }

  @Override
  public boolean onScaleBegin(ScaleGestureDetector detector) {
    mMode = Mode.ZOOM;
    return true;
  }

  @Override
  public void onScaleEnd(ScaleGestureDetector detector) {
    mIsScale = false;
  }


  @Override
  public void onDoubleTapBegin() {
    mIsDoubleTaping = true;
  }

  @Override
  public void onDoubleTapEnd() {
    mIsDoubleTaping = false;
  }

  @Override
  public void onDoubleTap(MotionEvent e) {
    final float startScale = mScaleFactor;
    float endScale = mScaleFactor;
    if (endScale - mDefaultScaleFactor < mMaxScaleFactor - endScale) {
      endScale = mMaxScaleFactor;
    } else {
      endScale = mDefaultScaleFactor;
    }

    adjustPosition2(startScale, endScale);
    mIsDoubleTaping = false;
  }



  class Float5Value {
    float scale;
    float x;
    float y;
    float centerX;
    float centerY;
  }

  public class AdjustAnimation extends Animation<Float5Value> {

    private Float5Value mPreValue;
    private float mCenterX, mCenterY;

    public void setAnimation(float startScale, float startX, float startY, float endScale, float endX, float endY) {
      if (DEBUG) {
        Log.i(TAG,
            String.format("setAnimation[[%f->%f],[%f->%f],[%f->%f]]", startScale, endScale, startX, endX, startY, endY));
      }
      Float5Value start = new Float5Value();
      start.scale = startScale;
      start.x = startX;
      start.y = startY;

      Float5Value end = new Float5Value();
      end.scale = endScale;
      end.x = endX;
      end.y = endY;

      setStartValue(start);
      setEndValue(end);


      mPreValue = start;
    }

    @Override
    public Float5Value getTransformationValue(float fraction, Float5Value start, Float5Value end) {
      updateCenterPoint();
      float scale = start.scale + (end.scale - start.scale) * fraction;
      float x = start.x + (end.x - start.x) * fraction;
      float y = start.y + (end.y - start.y) * fraction;

      Float5Value result = new Float5Value();
      result.x = x - mPreValue.x;
      result.y = y - mPreValue.y;
      result.scale = scale / mPreValue.scale;
      result.centerX = mCenterX;
      result.centerY = mCenterY;

      mPreValue.scale = scale;
      mPreValue.x = x;
      mPreValue.y = y;

      if (DEBUG) {
        Log.i(TAG, String.format("getTransformationValue[frac:%f,scale:%f,x:%f,y:%f,cx:%f,cy:%f]", fraction,
            result.scale, result.x, result.y, result.centerX, result.centerY));
      }
      return result;
    }

    protected void updateCenterPoint() {
      mDrawMatrix.getValues(mPos);
      mCenterX = mPos[2] + mPos[0] * mBitmapWidth / 2.0f;
      mCenterY = mPos[5] + mPos[4] * mBitmapHeight / 2.0f;
    }
  }



}
