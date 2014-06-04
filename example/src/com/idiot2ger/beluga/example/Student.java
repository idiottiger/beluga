package com.idiot2ger.beluga.example;

import android.content.Context;
import android.util.Log;

import com.idiot2ger.beluga.inject.Inject;
import com.idiot2ger.beluga.inject.Injector;
import com.idiot2ger.beluga.inject.Singleton;

@Singleton
public class Student {

  @Inject
  private Context mContext;

  private Student() {
    Log.i("TAG", "student init");
    Injector.inject(this);
  }

  public void printf() {
    Log.i("TAG", "student ..." + mContext.getPackageName());
  }

}
