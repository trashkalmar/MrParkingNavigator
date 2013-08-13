package ru.mail.parking.widget.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class NetworkAwaiter {
  private static final NetworkAwaiter sInstance = new NetworkAwaiter();
  public static NetworkAwaiter getInstance() {
    return sInstance;
  }

  private NetworkAwaiter() {}


  private boolean mNetworkAvailable = NetworkStateReceiver.isNetworkAvailable();
  private final Map<String, Runnable> mTasks = new HashMap<String, Runnable>();
  private final Set<String> mPending = new HashSet<String>();


  public synchronized void start(String key, Runnable task) {
    mTasks.put(key, task);
    if (!mPending.contains(key))
      if (mNetworkAvailable)
        Utils.runUi(mTasks.remove(key));
      else
        mPending.add(key);
  }

  public synchronized void onNetworkChanged(boolean hasNetwork) {
    boolean old = mNetworkAvailable;
    mNetworkAvailable = hasNetwork;

    if (mNetworkAvailable && !old) {
      for (String key: mPending)
        Utils.runUi(mTasks.remove(key));

      mPending.clear();
    }
  }

  public synchronized void cancelAll() {
    mTasks.clear();
    mPending.clear();
  }
}
