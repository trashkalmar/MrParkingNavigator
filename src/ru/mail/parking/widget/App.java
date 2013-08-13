package ru.mail.parking.widget;

import android.app.Application;

import ru.mail.parking.widget.utils.NetworkAwaiter;
import ru.mail.parking.widget.utils.NetworkStateReceiver;

public class App extends Application
              implements NetworkStateReceiver.NetworkChangedListener {
  private static App sInstance;
  private static Preferences sPrefs;


  private void watchNetwork(boolean watch) {
    if (watch)
      NetworkStateReceiver.register(this);
    else {
      NetworkAwaiter.getInstance().cancelAll();
      NetworkStateReceiver.unregister();
    }
  }

  @Override
  public void onCreate() {
    super.onCreate();
    sInstance = this;
    sPrefs = new Preferences();

    if (MainWidgetProvider.getWidgetIds().length != 0)
      start();
  }

  public static App app() {
    return sInstance;
  }

  public static Preferences prefs() {
    return sPrefs;
  }

  @Override
  public void onNetworkChanged(boolean hasNetwork) {
    NetworkAwaiter.getInstance().onNetworkChanged(hasNetwork);
  }

  public void start() {
    watchNetwork(true);
    SmartUpdate.force();
  }

  public void stop() {
    watchNetwork(false);
    SmartUpdate.abort();
  }
}