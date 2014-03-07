package com.idiot2ger.beluga.inject;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
/**
 * 
 * @author idiot2ger
 *
 */
public @interface InjectView {

  /**
   * the view id
   * 
   * @return
   */
  public int value() default -1;

}
