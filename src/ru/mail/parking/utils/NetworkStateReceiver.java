package ru.mail.parking.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import static ru.mail.parking.App.app;

public class NetworkStateReceiver extends BroadcastReceiver {
  public interface NetworkChangedListener {
    void onNetworkChanged(boolean hasNetwork);
  }

  private static NetworkChangedListener sListener;
  private static NetworkStateReceiver sInstance;

  private static final long TRIGGER_DELAY = 1000;
  private static final Runnable sTrigger = new Runnable() {
    @Override
    public void run() {
      if (sListener != null)
        sListener.onNetworkChanged(isNetworkAvailable());
    }
  };


  public static void register(NetworkChangedListener listener) {
    if (sInstance != null) return;
    sListener = listener;

    sInstance = new NetworkStateReceiver();
    app().registerReceiver(sInstance, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
  }

  public static void unregister() {
    if (sInstance == null) return;

    app().unregisterReceiver(sInstance);
    sInstance = null;

    Utils.cancelUiTask(sTrigger);
    sListener = null;
  }

  public static boolean isNetworkAvailable() {
    ConnectivityManager cm = (ConnectivityManager)app().getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo ni = cm.getActiveNetworkInfo();
    return (ni != null && ni.isConnected());
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    Utils.cancelUiTask(sTrigger);
    Utils.runUiLater(sTrigger, TRIGGER_DELAY);
  }
}
