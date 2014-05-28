package com.idiot2ger.beluga.animation;

import android.view.animation.Interpolator;


/**
 * 
 * @author r2d2
 * 
 * @param <T>
 */
interface IAnimationModel<T> {

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


}
