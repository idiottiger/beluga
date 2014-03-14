package com.idiot2ger.beluga.inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Application;
import android.app.DownloadManager;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.app.UiModeManager;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;


/**
 * injector core class</p> <b>How to use ?</b> </p> <li>1.use {@link #initializeInject(Application)}
 * to init the injector, suggestion invoke in the {@link Application#onCreate()} method</li> <li>
 * 2.if you want to customise your own inject object, you can use
 * {@link #putGlobalInject(Class, Object)} to add, but currently only support singleton instance</li>
 * <li>3.use {@link Inject} or {@link InjectView} or {@link Singleton} to your field or class
 * declare</li><li>4.before use the field, you need invoke {@link #inject(Object)} or
 * {@link #inject(Object, Object)} to inject</li> <li>5.you can use {@link #destroyInject()} to
 * release all</li></p>
 * 
 * @author idiot2ger
 * @see Inject
 * @see InjectView
 * @see Singleton
 */
public final class Injector {

  private Injector() {

  }

  private final static Map<Class<?>, HashSet<InjectItem>> sInjectItemMap = new HashMap<Class<?>, HashSet<InjectItem>>();
  private final static Map<Class<?>, Object> sGlobalInjectMap = new HashMap<Class<?>, Object>();


  /**
   * put the global inject object to the map, you can insert your object, when use @Inject it can
   * inject from the map
   * 
   * @param cls
   * @param object
   */
  public static void putGlobalInject(Class<?> cls, Object object) {
    sGlobalInjectMap.put(cls, object);
  }

  public static void inject(Object object) {
    inject(object, object);
  }

  static void preloadInject(Application application) {
    preloadServiceInject(application);
    putGlobalInject(Context.class, application.getApplicationContext());
    putGlobalInject(AssetManager.class, application.getAssets());
    putGlobalInject(Resources.class, application.getResources());
  }

  /**
   * injector init
   * 
   * @param application
   */
  public static void initializeInject(Application application) {
    preloadInject(application);
  }

  public static void destroyInject() {
    sInjectItemMap.clear();
    sGlobalInjectMap.clear();
  }

  private static void preloadServiceInject(Context context) {
    final Map<Class<?>, String> serviceMap = new HashMap<Class<?>, String>();

    serviceMap.put(WindowManager.class, Context.WINDOW_SERVICE);
    serviceMap.put(LayoutInflater.class, Context.LAYOUT_INFLATER_SERVICE);
    serviceMap.put(ActivityManager.class, Context.ACTIVITY_SERVICE);
    serviceMap.put(PowerManager.class, Context.POWER_SERVICE);
    serviceMap.put(AlarmManager.class, Context.ALARM_SERVICE);
    serviceMap.put(NotificationManager.class, Context.NOTIFICATION_SERVICE);
    serviceMap.put(KeyguardManager.class, Context.KEYGUARD_SERVICE);
    serviceMap.put(SearchManager.class, Context.SEARCH_SERVICE);
    serviceMap.put(Vibrator.class, Context.VIBRATOR_SERVICE);
    serviceMap.put(ConnectivityManager.class, Context.CONNECTIVITY_SERVICE);
    serviceMap.put(WifiManager.class, Context.WIFI_SERVICE);
    serviceMap.put(ConnectivityManager.class, Context.CONNECTIVITY_SERVICE);
    serviceMap.put(InputMethodManager.class, Context.INPUT_METHOD_SERVICE);
    serviceMap.put(UiModeManager.class, Context.UI_MODE_SERVICE);
    serviceMap.put(DownloadManager.class, Context.DOWNLOAD_SERVICE);

    Set<Entry<Class<?>, String>> entrySet = serviceMap.entrySet();
    for (Entry<Class<?>, String> entry : entrySet) {
      sGlobalInjectMap.put(entry.getKey(), context.getSystemService(entry.getValue()));
    }
    serviceMap.clear();
  }


  public static void inject(Object object, Object injectProvider) {
    if (object == null) {
      throw new IllegalArgumentException("when inject, the object must not be null");
    }
    Class<?> cls = object.getClass();
    // search two maps

    HashSet<InjectItem> injectSet = null;
    if (sInjectItemMap.containsKey(cls)) {
      injectSet = sInjectItemMap.get(cls);
    } else {
      // query all inject item to the maps
      Field[] fieldList = cls.getDeclaredFields();
      if (fieldList != null && fieldList.length != 0) {
        final HashSet<InjectItem> set = new HashSet<InjectItem>();
        for (Field field : fieldList) {
          InjectItem item = null;
          if (field.isAnnotationPresent(InjectView.class)) {
            item = new InjectViewItem(field, field.getAnnotation(InjectView.class));
          } else if (field.isAnnotationPresent(Inject.class)) {
            // check the field class type's annotation
            final Class<?> fieldType = field.getType();

            // find in the global inject
            // global inject search first
            if (sGlobalInjectMap.containsKey(fieldType)) {
              setField(field, object, sGlobalInjectMap.get(fieldType));
              continue;
            } else if (fieldType.isAnnotationPresent(Singleton.class)) {
              item = new InjectSingletonItem(field);
            }
          }
          if (item != null) {
            set.add(item);
          }
        }

        if (!set.isEmpty()) {
          sInjectItemMap.put(cls, set);
          injectSet = set;
        }
      }
    }

    if (injectSet != null) {
      for (InjectItem item : injectSet) {
        item.initField(object, injectProvider);
      }
    }
  }

  private static void setField(Field field, Object caller, Object value) {
    try {
      field.setAccessible(true);
      field.set(caller, field.getType().cast(value));
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
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

  private static class InjectSingletonItem extends InjectItem {

    private Class<?> cls;

    public InjectSingletonItem(Field f) {
      super(f);
      cls = f.getType();

      if (!sGlobalInjectMap.containsKey(cls)) {
        // check
        Constructor<?>[] conList = cls.getDeclaredConstructors();
        if (conList != null && conList.length > 0) {
          boolean find = false;
          Constructor<?> constructor = null;
          for (Constructor<?> con : conList) {
            if (con.getModifiers() == Modifier.PRIVATE && con.getParameterTypes().length == 0) {
              find = true;
              constructor = con;
              break;
            }
          }
          if (!find) {
            throw new IllegalArgumentException(cls.getName()
                + " with the @Singleton must has a private and non-arg class constructor");
          }

          // init it
          constructor.setAccessible(true);
          try {
            Object value = cls.cast(constructor.newInstance((Object[]) null));
            sGlobalInjectMap.put(cls, value);
          } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
      }
    }

    @Override
    public void initField(Object caller, Object object) {
      if (sGlobalInjectMap.containsKey(cls)) {
        try {
          field.setAccessible(true);
          field.set(caller, sGlobalInjectMap.get(cls));
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        } catch (IllegalArgumentException e) {
          e.printStackTrace();
        }
      }
    }

  }
}
