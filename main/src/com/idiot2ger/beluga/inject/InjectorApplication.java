package com.idiot2ger.beluga.inject;

import android.app.Application;

public class InjectorApplication extends Application {

  private Injector mInjector;

  @Override
  public void onCreate() {
    super.onCreate();

    mInjector = Injector.getInstance();
  }

  public void inject(Object object) {
    mInjector.inject(object);
  }

  public void inject(Object object, Object injectProvider) {
    mInjector.inject(object, injectProvider);
  }
}
