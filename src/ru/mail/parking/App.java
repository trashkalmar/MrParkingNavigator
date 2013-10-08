package ru.mail.parking;

import android.app.Application;

import ru.mail.parking.floors.FloorNavigator;
import ru.mail.parking.widget.MainWidgetProvider;
import ru.mail.parking.widget.SmartUpdate;
import ru.mail.parking.utils.NetworkAwaiter;
import ru.mail.parking.utils.NetworkStateReceiver;

public class App extends Application
              implements NetworkStateReceiver.NetworkChangedListener {
  private static App sInstance;
  private static Preferences sPrefs;
  private static FloorNavigator sFloors;


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
    sFloors = new FloorNavigator();

    if (MainWidgetProvider.getWidgetIds().length != 0)
      start();
  }

  public static App app() {
    return sInstance;
  }

  public static Preferences prefs() {
    return sPrefs;
  }

  public static FloorNavigator floors() {
    return sFloors;
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