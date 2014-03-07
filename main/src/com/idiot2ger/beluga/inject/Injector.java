package com.idiot2ger.beluga.inject;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import android.app.Activity;
import android.view.View;


/**
 * injector
 * 
 * @author idiot2ger
 * 
 */
public class Injector {

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
        item.initField(object, object);
      }
    }
  }


  private abstract class InjectItem {
    protected Field field;

    public InjectItem(Field f) {
      field = f;
    }

    public abstract void initField(Object caller, Object object);
  }

  private class InjectViewItem extends InjectItem {

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

}
