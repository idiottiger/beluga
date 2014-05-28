package com.idiot2ger.beluga.animation;

import com.idiot2ger.beluga.animation.Animation.State;

/**
 * 
 * @author r2d2
 * 
 * @param <T>
 */
public interface AnimationCallback<T> {

  /**
   * animation state change callback, there are: {@link State#STATE_START},{@link State#STATE_PAUSE}
   * ,{@link State#STATE_RESUME},{@link State#STATE_STOP},{@link State#STATE_END},
   * {@link State#STATE_RESET}
   * 
   * @param state
   */
  public void onAnimationStateChanged(State state);

  /**
   * when animation is running
   * 
   * @param fraction the time percent, the value [0.0-1.0]
   * @param value animation value
   */
  public void onAnimationing(float fraction, T value);
}
