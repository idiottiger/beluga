package com.idiot2ger.beluga.example;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.idiot2ger.beluga.messagebus.MessageAsyncHandle;
import com.idiot2ger.beluga.messagebus.MessageBus;
import com.idiot2ger.beluga.messagebus.MessageHandle;

public class MainActivity extends Activity {

  static final int MSG_1 = 1;
  static final int MSG_2 = 2;
  static final int MSG_3 = 3;

  static final String TAG = "MainActivity";

  private MessageBus mBus = MessageBus.getInstance();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  protected void onResume() {
    super.onResume();
    mBus.register(this);

    mBus.post(MSG_1);
    mBus.post(MSG_2, "hello world", 5000);
    mBus.post(MSG_3, 120000, 8000);
  }

  @MessageHandle(messageId = MSG_1)
  public void onGetMessage1() {
    Log.i(TAG, "onGetMessage1:" + MSG_1 + ", thread:" + Thread.currentThread().getName());
  }

  @MessageHandle(messageId = MSG_1)
  public void onGetMessage2() {
    Log.i(TAG, "onGetMessage2:" + MSG_1 + ", thread:" + Thread.currentThread().getName());
  }

  @MessageHandle(messageId = MSG_2)
  public void onGetMessage3(String content) {
    Log.i(TAG, "onGetMessage3:" + MSG_2 + ", thread:" + Thread.currentThread().getName());
    Log.i(TAG, "content:" + content);
  }

  @MessageAsyncHandle(messageId = MSG_3)
  public void onGetInt(Integer i) {
    Log.i(TAG, "onGetInt: thread:" + Thread.currentThread().getName() + ",content:" + i);
  }

  @Override
  protected void onStop() {
    super.onStop();
    mBus.unRegister(this);
  }
}
