package ru.mail.parking.widget;

import android.app.Application;

import ru.mail.parking.widget.utils.NetworkAwaiter;
import ru.mail.parking.widget.utils.NetworkStateReceiver;

public class App extends Application
              implements NetworkStateReceiver.NetworkChangedListener {
  private static App sInstance;
  private static Preferences sPrefs;

  @Override
  public void onCreate() {
    super.onCreate();
    sInstance = this;
    sPrefs = new Preferences();
  }

  public static App app() {
    return sInstance;
  }

  public static Preferences prefs() {
    return sPrefs;
  }

  public void watchNetwork(boolean watch) {
    if (watch)
      NetworkStateReceiver.register(this);
    else {
      NetworkAwaiter.getInstance().cancelAll();
      NetworkStateReceiver.unregister();
    }
  }

  @Override
  public void onNetworkChanged(boolean hasNetwork) {
    NetworkAwaiter.getInstance().onNetworkChanged(hasNetwork);
  }
}