package com.idiot2ger.beluga.inject;

import android.app.Application;
import android.content.Context;

public class InjectApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    // save context to the global
    Injector.putGlobalInject(Context.class, getApplicationContext());
  }



}
