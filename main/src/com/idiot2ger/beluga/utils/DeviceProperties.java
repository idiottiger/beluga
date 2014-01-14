package com.idiot2ger.beluga.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * some basic device properties, like screen size, every app limits memory, platform number, ect...
 * 
 * @author idiottiger
 * @version 1.0
 * 
 */
public class DeviceProperties {

  /**
   * screen width, the width depend on the orientation, before use it, need
   * {@link #getProperties(Context)} to return a new one
   */
  public int screenWidth;

  /**
   * screen height, the height depend on the orientation, before use it, need
   * {@link #getProperties(Context)} to return a new one
   */
  public int screenHeight;

  /**
   * screen density
   */
  public int density;

  /**
   * every app memory limits, like 16M, 24M...
   */
  public int perAppMemoryLimit;

  /**
   * android platform version code, like: 8 = Froyo, etc...
   */
  public static final int androidVersion = Build.VERSION.SDK_INT;

  /**
   * external storage path, like /mnt/sdcard
   */
  public static String externalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();

  DeviceProperties() {}

  /**
   * only method to return the properties
   * 
   * @param context
   * @return
   */
  public static DeviceProperties getProperties(Context context) {
    DeviceProperties properties = new DeviceProperties();

    DisplayMetrics dm = new DisplayMetrics();
    WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    manager.getDefaultDisplay().getMetrics(dm);
    properties.screenWidth = dm.widthPixels;
    properties.screenHeight = dm.heightPixels;
    properties.density = dm.densityDpi;

    ActivityManager aManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    properties.perAppMemoryLimit = aManager.getMemoryClass();

    return properties;
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("device properties:\n");
    sb.append(" screenWidth: " + screenWidth + ", screenHeight: " + screenHeight + "\n");
    sb.append(" density: " + density + "\n");
    sb.append(" every app memory limit: " + perAppMemoryLimit + "M\n");
    sb.append(" android version: " + androidVersion);
    return sb.toString();
  }

}
