package ru.mail.parking;

import android.app.Application;

import ru.mail.parking.floors.FloorNavigator;
import ru.mail.parking.utils.NetworkAwaiter;
import ru.mail.parking.utils.NetworkStateReceiver;
import ru.mail.parking.widget.MainWidgetProvider;
import ru.mail.parking.widget.SmartUpdate;

public class App extends Application
              implements NetworkStateReceiver.NetworkChangedListener {
  private static App sInstance;
  private static Preferences sPrefs;
  private static FloorNavigator sFloors;

  private boolean mHasWidgets;
  private boolean mHasSmartwatch;
  private boolean mRunning;


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

    setHasWidgets(MainWidgetProvider.getWidgetIds().length != 0);
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

  private void onConsumersChanged() {
    boolean run = (mHasWidgets || mHasSmartwatch);
    if (run == mRunning)
      return;

    mRunning = run;
    watchNetwork(mRunning);

    if (mRunning)
      SmartUpdate.force();
    else
      SmartUpdate.abort();
  }

  public void setHasWidgets(boolean hasWidgets) {
    mHasWidgets = hasWidgets;
    onConsumersChanged();
  }

  public void setHasSmartwatch(boolean hasSmartwatch) {
    mHasSmartwatch = hasSmartwatch;
    onConsumersChanged();
  }
}