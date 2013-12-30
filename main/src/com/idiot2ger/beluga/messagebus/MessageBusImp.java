package com.idiot2ger.beluga.messagebus;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;

/**
 * 
 * @author idiot2ger
 * 
 */
public final class MessageBusImp implements IMessageBus {


  // all class cache
  private Set<Class<?>> mClassCache = new HashSet<Class<?>>();

  // current register object cache, if unreigster, will remove this object from set
  private Map<Class<?>, Set<Object>> mObjectCache = new HashMap<Class<?>, Set<Object>>();

  // method cache
  private SparseArray<List<MethodProcessor>> mProcessorCache = new SparseArray<List<MethodProcessor>>();

  private static MessageBusImp mInstance;

  private Handler mHandler;

  /**
   * get message bus instance
   * 
   * @return
   */
  public static MessageBusImp getInstance() {
    if (mInstance == null) {
      mInstance = new MessageBusImp();
    }
    return mInstance;
  }

  @SuppressLint("HandlerLeak")
  private MessageBusImp() {
    mHandler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
        processMessage(msg);
      }
    };
  }

  @Override
  public synchronized void register(Object object) {
    if (object == null) {
      throw new IllegalArgumentException("register to the message bus, the object can not be NULL");
    }
    findAnnotationAndCache(object);
  }

  @Override
  public synchronized void unRegister(Object object) {
    if (object == null) {
      throw new IllegalArgumentException("unRegister to the message bus, the object can not be NULL");
    }
    // remove from object cache
    Collection<Set<Object>> objSets = mObjectCache.values();
    for (Set<Object> set : objSets) {
      set.remove(object);
    }
  }

  private void processMessage(final Message message) {

  }

  @Override
  public void post(int messageId) {
    // TODO Auto-generated method stub

  }

  @Override
  public void post(int messageId, long delay) {
    // TODO Auto-generated method stub

  }

  @Override
  public void post(int messageId, Object object) {
    // TODO Auto-generated method stub

  }

  @Override
  public void post(int messageId, Object object, long delay) {
    // TODO Auto-generated method stub

  }

  @Override
  public void postImmediate(int messageId) {
    // TODO Auto-generated method stub

  }

  @Override
  public void postImmediate(int messageId, Object object) {
    // TODO Auto-generated method stub

  }

  @Override
  public Object postImmediate2(int messageId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Object postImmediate2(int messageId, Object object) {
    // TODO Auto-generated method stub
    return null;
  }


  private void findAnnotationAndCache(Object object) {
    final Class<?> cls = object.getClass();
    // search current cache
    if (!mClassCache.contains(cls)) {
      final Method[] methods = cls.getMethods();
      if (methods != null) {
        boolean isMH, isAMH;
        for (Method method : methods) {
          isMH = method.isAnnotationPresent(MessageHandle.class);
          isAMH = method.isAnnotationPresent(MessageAsyncHandle.class);
          if (isMH && isAMH) {
            // if method have MessageHandle and MessageAsyncHandle will throws exception
            throw new RuntimeException("class:" + cls.getName() + ", method:" + method.getName()
                + " cannot has MessageHandle and MessageAsyncHandle annotations at same time");
          } else {
            if (isMH || isAMH) {
              // create the processor
              final MethodProcessor processor = new MethodProcessor();
              processor.cls = cls;
              if (isMH) {
                processor.messageId = method.getAnnotation(MessageHandle.class).messageId();
              } else if (isAMH) {
                processor.messageId = method.getAnnotation(MessageAsyncHandle.class).messageId();
              }
              processor.method = method;
              processor.isAsync = isAMH;

              List<MethodProcessor> processorList = mProcessorCache.get(processor.messageId);
              if (processorList == null) {
                processorList = new ArrayList<MessageBusImp.MethodProcessor>();
                mProcessorCache.put(processor.messageId, processorList);
              }

              if (!processorList.contains(processor)) {
                processorList.add(processor);
              }
            }
          }
        }
      }
    }

    // add object to cache
    Set<Object> objectSet = mObjectCache.get(cls);
    if (objectSet == null) {
      objectSet = new HashSet<Object>();
      mObjectCache.put(cls, objectSet);
    }
    objectSet.add(object);
  }

  class MethodProcessor {
    Class<?> cls;
    int messageId;
    Method method;
    boolean isAsync;

    @Override
    public boolean equals(Object o) {
      if (o instanceof MethodProcessor) {
        return cls.equals(o.getClass()) && method.equals(((MethodProcessor) o).method);
      }
      return false;
    }

  }
}
