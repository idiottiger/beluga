package com.idiot2ger.beluga.example;

import com.idiot2ger.beluga.inject.Injector;

import android.app.Application;

public class MyApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();

    Injector.initializeInject(this);
  }
}
