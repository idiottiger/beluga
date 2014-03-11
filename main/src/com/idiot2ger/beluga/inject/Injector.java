package com.idiot2ger.beluga.inject;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.DownloadManager;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.app.UiModeManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;


/**
 * injector
 * 
 * @author idiot2ger
 * 
 */
class Injector {

  private Injector() {

  }

  private static Injector sInjector;


  private Map<Class<?>, HashSet<InjectItem>> mInjectItemMap = new HashMap<Class<?>, HashSet<InjectItem>>();

  public static final Injector getInstance() {
    if (sInjector == null) {
      sInjector = new Injector();
    }
    return sInjector;
  }

  public void inject(Object object) {
    inject(object, object);
  }


  public void inject(Object object, Object injectProvider) {
    if (object == null) {
      throw new IllegalArgumentException("when inject, the object must not be null");
    }
    Class<?> cls = object.getClass();
    // search two maps

    HashSet<InjectItem> injectSet = null;
    if (mInjectItemMap.containsKey(cls)) {
      injectSet = mInjectItemMap.get(cls);
    } else {
      // query all inject item to the maps
      Field[] fieldList = cls.getDeclaredFields();
      if (fieldList != null && fieldList.length != 0) {
        final HashSet<InjectItem> set = new HashSet<InjectItem>();
        for (Field field : fieldList) {
          InjectItem item = null;
          if (field.isAnnotationPresent(InjectView.class)) {
            item = new InjectViewItem(field, field.getAnnotation(InjectView.class));
          } else if (field.isAnnotationPresent(InjectService.class)) {
            item = new InjectServiceItem(field);
          }
          if (item != null) {
            set.add(item);
          }
        }
        mInjectItemMap.put(cls, set);
        injectSet = set;
      }
    }

    if (injectSet != null) {
      for (InjectItem item : injectSet) {
        item.initField(object, injectProvider);
      }
    }
  }


  private static abstract class InjectItem {
    protected Field field;

    public InjectItem(Field f) {
      field = f;
    }

    public abstract void initField(Object caller, Object object);
  }

  private static class InjectViewItem extends InjectItem {

    private int id;

    public InjectViewItem(Field f, InjectView injectView) {
      super(f);
      id = injectView.value();
      if (id == -1) {
        throw new IllegalArgumentException("InjectView must has a 'id', and must not equal -1");
      }
    }

    @Override
    public void initField(Object caller, Object object) {
      Object value = null;
      if (object instanceof Activity) {
        value = ((Activity) object).findViewById(id);
      } else if (object instanceof View) {
        value = ((View) object).findViewById(id);
      } else {
        throw new IllegalArgumentException("InjectView: when Inject init field:" + field.getName()
            + ", the object need instance of Activity or View");
      }
      try {
        field.setAccessible(true);
        field.set(caller, field.getType().cast(value));
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (IllegalArgumentException e) {
        e.printStackTrace();
      }
    }
  }

  private static class InjectServiceItem extends InjectItem {

    private static HashMap<Class<?>, String> sServiceMap = new HashMap<Class<?>, String>();

    static {
      sServiceMap.put(WindowManager.class, Context.WINDOW_SERVICE);
      sServiceMap.put(LayoutInflater.class, Context.LAYOUT_INFLATER_SERVICE);
      sServiceMap.put(ActivityManager.class, Context.ACTIVITY_SERVICE);
      sServiceMap.put(PowerManager.class, Context.POWER_SERVICE);
      sServiceMap.put(AlarmManager.class, Context.ALARM_SERVICE);
      sServiceMap.put(NotificationManager.class, Context.NOTIFICATION_SERVICE);
      sServiceMap.put(KeyguardManager.class, Context.KEYGUARD_SERVICE);
      sServiceMap.put(SearchManager.class, Context.SEARCH_SERVICE);
      sServiceMap.put(Vibrator.class, Context.VIBRATOR_SERVICE);
      sServiceMap.put(ConnectivityManager.class, Context.CONNECTIVITY_SERVICE);
      sServiceMap.put(WifiManager.class, Context.WIFI_SERVICE);
      sServiceMap.put(ConnectivityManager.class, Context.CONNECTIVITY_SERVICE);
      sServiceMap.put(InputMethodManager.class, Context.INPUT_METHOD_SERVICE);
      sServiceMap.put(UiModeManager.class, Context.UI_MODE_SERVICE);
      sServiceMap.put(DownloadManager.class, Context.DOWNLOAD_SERVICE);
    }

    public InjectServiceItem(Field f) {
      super(f);
    }

    @Override
    public void initField(Object caller, Object object) {
      Class<?> serviceType = field.getType();
      String serviceName = sServiceMap.get(serviceType);
      if (serviceName != null) {
        try {
          field.setAccessible(true);
          Object value = ((Context) object).getSystemService(serviceName);
          field.set(caller, field.getType().cast(value));
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        } catch (IllegalArgumentException e) {
          e.printStackTrace();
        }
      }
    }

  }

}
