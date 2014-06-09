package com.idiot2ger.beluga.animation;

import android.view.animation.Interpolator;

/**
 * 
 * @author r2d2
 * 
 * @param <T>
 */
public interface IAnimation<T> {

  /**
   * animation state
   * 
   * @author r2d2
   * 
   */
  public static enum State {
    STATE_NONE, STATE_START, STATE_PAUSE, STATE_RESUME, STATE_STOP, STATE_END, STATE_RESET
  }

  /**
   * animation id
   * 
   * @return
   */
  public int getAnimationId();

  /**
   * animation running
   */
  public void animationRunning();

  /**
   * new animation state callback
   * 
   * @param state
   * @return true or false, true set ok, otherwise failture
   */
  public boolean updateAnimationState(State state);

  /**
   * get the transformation value
   * 
   * @param fraction, value [0.0-1.0], and it's value already calculate with the
   *        {@link Interpolator}
   * @param start
   * @param end
   * @return
   */
  public T getTransformationValue(float fraction, T start, T end);

  /**
   * start animation
   */
  public void start();

  /**
   * pause animation
   */
  public void pause();

  /**
   * resume animation
   */
  public void resume();

  /**
   * stop animation
   */
  public void stop();

  /**
   * reset animation
   */
  public void reset();

  /**
   * release animation
   */
  public void release();

  /**
   * get current animation state
   * 
   * @return
   */
  public State getAnimationState();

}
