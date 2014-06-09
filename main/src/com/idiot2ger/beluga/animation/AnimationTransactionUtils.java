package com.idiot2ger.beluga.animation;

import com.idiot2ger.beluga.animation.Animation;

public class AnimationTransactionUtils {

  private static final int SEQUENCE_TYPE = 1;
  private static final int PARALLE_TYPE = 2;

  public static IAnimationTransaction newSequenceAnimations(Animation<?>... animations) {
    return newAnimationTransaction(SEQUENCE_TYPE, animations);
  }

  public static IAnimationTransaction newParallelAnimations(Animation<?>... animations) {
    return newAnimationTransaction(PARALLE_TYPE, animations);
  }

  private static IAnimationTransaction newAnimationTransaction(int type, Animation<?>... animations) {
    if (animations == null) {
      return null;
    }
    AbstractAnimationTransaction transaction =
        (type == SEQUENCE_TYPE) ? (new AnimationSequenceTransaction()) : (new AnimationParalleTransaction());
    for (Animation<?> animation : animations) {
      transaction.addAnimation(animation);
    }
    return transaction;
  }

}
