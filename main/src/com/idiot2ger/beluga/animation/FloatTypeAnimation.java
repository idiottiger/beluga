package com.idiot2ger.beluga.animation;

class FloatTypeAnimation extends Animation<Float> {

  @Override
  public Float getTransformationValue(float fraction, Float start, Float end) {
    return start + (end - start) * fraction;
  }

}
