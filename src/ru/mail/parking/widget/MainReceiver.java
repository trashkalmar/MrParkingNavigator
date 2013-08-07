package ru.mail.parking.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ru.mail.parking.widget.ui.DataDetailsActivity;

public class MainReceiver extends BroadcastReceiver {
  public static final String ACTION_CLICK = "ru.mail.parking.widget.CLICK";
  public static final String ACTION_UPDATE = "ru.mail.parking.widget.SMART_UPDATE";


  public void onReceive(Context context, Intent intent) {
    if (ACTION_CLICK.equals(intent.getAction())) {
      switch (App.prefs().getClickAction()) {
        case update:
          SmartUpdate.force();
          break;

        case details:
          context.startActivity(new Intent(context, DataDetailsActivity.class)
                                   .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
          break;
      }
    } else


    if (Intent.ACTION_TIME_CHANGED.equals(intent.getAction()) ||
        Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())) {
      SmartUpdate.onTimeChanged();
    } else


    if (ACTION_UPDATE.equals(intent.getAction())) {
      SmartUpdate.onAlarm();
    }
  }
}
