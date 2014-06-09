package com.idiot2ger.beluga.animation;


/**
 * 
 * @author r2d2
 * 
 * @param <T>
 */
public interface AnimationCallback<T> {

  /**
   * when animation is running
   * 
   * @param fraction the time percent, the value [0.0-1.0]
   * @param value animation value
   */
  public void onAnimationing(float fraction, T value);
}
