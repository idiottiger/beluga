package com.idiot2ger.beluga.inject;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import android.view.View.OnClickListener;

/**
 * onclick inject for view onclick event, you can define like this:
 * 
 * <pre>
 * @OnClick({R.id.xxx,R.id.xxx,...})
 * OnClickListener mListener; //mListener must init before invoke Inject.inject() method
 * </pre>
 * 
 * @author idiot2ger
 * @see OnClickListener
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OnClick {

  /** the layout id list **/
  public int[] value() default -1;
}
