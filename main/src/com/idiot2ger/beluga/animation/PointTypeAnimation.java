package com.idiot2ger.beluga.animation;

class PointTypeAnimation extends Animation<Point> {

  @Override
  public Point getTransformationValue(float fraction, Point start, Point end) {
    float nx = start.x + (end.x - start.x) * fraction;
    float ny = start.y + (end.y - start.y) * fraction;
    return new Point(nx, ny);
  }

}
