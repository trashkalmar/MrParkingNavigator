package ru.mail.parking.widget;

import android.app.Application;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class App extends Application {
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

  public static String formatDate(Calendar c, Preferences.TimeFormat format) {
    SimpleDateFormat sdf = new SimpleDateFormat(format.getFormatString());
    return sdf.format(c.getTime());
  }

  public static String formatDate(long tm, Preferences.TimeFormat format, boolean forceTz) {
    if (tm == 0) return app().getString(R.string.unknown);

    Calendar c = Calendar.getInstance();
    if (forceTz)
      c.setTimeZone(TimeZone.getTimeZone("GMT+0400"));

    c.setTimeInMillis(tm);

    return formatDate(c, format);
  }
}