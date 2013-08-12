package ru.mail.parking.widget.utils;

import android.os.Handler;
import android.os.Looper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import ru.mail.parking.widget.App;
import ru.mail.parking.widget.Preferences;
import ru.mail.parking.widget.R;

public final class Utils {
  private static Handler sUiHandler = new Handler(Looper.getMainLooper());

  private Utils() {}


  public static String formatDate(Calendar c, Preferences.TimeFormat format) {
    SimpleDateFormat sdf = new SimpleDateFormat(format.getFormatString());
    return sdf.format(c.getTime());
  }

  public static String formatDate(long tm, Preferences.TimeFormat format, boolean forceTz) {
    if (tm == 0)
      return App.app().getString(R.string.unknown);

    Calendar c = Calendar.getInstance();
    if (forceTz)
      c.setTimeZone(TimeZone.getTimeZone("GMT+0400"));

    c.setTimeInMillis(tm);
    return formatDate(c, format);
  }

  public static void runUi(Runnable task) {
    if (sUiHandler.getLooper().getThread() == Thread.currentThread())
      task.run();
    else
      sUiHandler.post(task);
  }

  public static void runUiLater(Runnable task, long delay) {
    sUiHandler.postDelayed(task, delay);
  }

  public static void cancelUiTask(Runnable task) {
    sUiHandler.removeCallbacks(task);
  }
}