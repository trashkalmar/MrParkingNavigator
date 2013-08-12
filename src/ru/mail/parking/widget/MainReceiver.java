package ru.mail.parking.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ru.mail.parking.widget.ui.DataDetailsActivity;
import ru.mail.parking.widget.ui.SettingsActivity;
import ru.mail.parking.widget.utils.Utils;

import static ru.mail.parking.widget.App.app;

public class MainReceiver extends BroadcastReceiver {
  public static final String ACTION_TAP = "ru.mail.parking.widget.TAP";
  public static final String ACTION_UPDATE = "ru.mail.parking.widget.SMART_UPDATE";

  private static final long TAP_DELAY = 300;

  private static long sLastTap;

  private static final Runnable sTapRunner = new Runnable() {
    @Override
    public void run() {
      executeClickAction(App.prefs().getClickAction());
    }
  };


  private static void executeClickAction(Preferences.ClickAction action) {
    switch (action) {
      case update:
        SmartUpdate.force();
        break;

      case details:
        app().startActivity(new Intent(app(), DataDetailsActivity.class)
                               .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        break;

      case settings:
        app().startActivity(new Intent(app(), SettingsActivity.class)
                               .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        break;
    }
  }

  public void onReceive(Context context, Intent intent) {
    if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
      if (MainWidgetProvider.getWidgetIds().length != 0)
        SmartUpdate.force();
    } else


    if (ACTION_TAP.equals(intent.getAction())) {
      long now = System.currentTimeMillis();
      long diff = now - sLastTap;

      if (diff > 0 && diff < TAP_DELAY) {
        Utils.cancelUiTask(sTapRunner);
        executeClickAction(App.prefs().getDoubletapAction());
        return;
      }

      sLastTap = now;
      Utils.runUiLater(sTapRunner, TAP_DELAY);
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
