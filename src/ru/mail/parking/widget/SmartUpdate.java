package ru.mail.parking.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public final class SmartUpdate {
  private static AlarmManager mManager;


  static {
    mManager = (AlarmManager)App.app().getSystemService(Context.ALARM_SERVICE);
  }

  private SmartUpdate() {}

  private static PendingIntent createActionIntent() {
    Intent it = new Intent(App.app(), MainReceiver.class)
                   .setAction(MainReceiver.ACTION_UPDATE);

    return PendingIntent.getBroadcast(App.app(), 0, it, PendingIntent.FLAG_CANCEL_CURRENT);
  }


  public static void force() {
    abort();
    UpdateService.start();
  }

  public static void abort() {
    PendingIntent pi = createActionIntent();
    mManager.cancel(pi);
    pi.cancel();
  }

  public static void onAlarm() {

  }

  public static void onTimeChanged() {

  }
}