package com.idiot2ger.beluga.messagebus;

import java.lang.reflect.Method;

import org.apache.http.MethodNotSupportedException;

/**
 * 
 * @author idiot2ger
 * 
 */
public final class MessageBusImp implements IMessageBus {


  private static MessageBusImp mInstance;

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

  private MessageBusImp() {

  }


  @Override
  public void register(Object object) {
    // TODO Auto-generated method stub

  }

  @Override
  public void unRegister(Object object) {
    // TODO Auto-generated method stub

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
    final Method[] methods = cls.getMethods();
    if (methods != null) {
      for (Method method : methods) {
        if (method.isAnnotationPresent(MessageHandle.class) && method.isAnnotationPresent(MessageAsyncHandle.class)) {
          // if method have MessageHandle and MessageAsyncHandle will throws exception
          throw new RuntimeException("class:" + cls.getName() + ", method:" + method.getName()
              + " cannot has MessageHandle and MessageAsyncHandle annotations at same time");
        } else {
          if (method.isAnnotationPresent(MessageHandle.class)) {
            
          }
        }
      }
    }
  }
}
