package com.idiot2ger.beluga.messagebus;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
/**
 * message handle annotation
 * @author idiot2ger
 *
 */
public @interface MessageHandle {

  /**
   * the message id
   * 
   * @return
   */
  public int messageId() default 0;
}
