package com.idiot2ger.beluga.inject;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

public class InjectApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    // preload
    Injector.preloadInject(this);

    // global inject init
    Injector.putGlobalInject(Context.class, getApplicationContext());
    Injector.putGlobalInject(AssetManager.class, getAssets());
    Injector.putGlobalInject(Resources.class, getResources());

  }


}
