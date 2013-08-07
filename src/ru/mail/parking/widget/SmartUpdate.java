package ru.mail.parking.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

public final class SmartUpdate {
  private static AlarmManager mManager;

  public enum Policy {
    auto {
      @Override
      public void schedule() {
        PendingIntent pi = createActionIntent();
        mManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 60 * 60000, pi);
      }
    },

    manual {
      @Override
      public void schedule() {
        // Do nothing here
      }
    },

    smart {
      @Override
      public void schedule() {
        // TODO
      }
    };


    private static Policy sDefault;

    public static Policy getDefault() {
      if (sDefault == null)
        sDefault = valueOf(App.app().getString(R.string.prefs_config_update_mode_default));

      return sDefault;
    }


    public abstract void schedule();
  }


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
    UpdateService.start(true);
    schedule();
  }

  public static void abort() {
    PendingIntent pi = createActionIntent();
    mManager.cancel(pi);
    pi.cancel();
  }

  public static void schedule() {
    App.prefs().getUpdatePolicy().schedule();
  }

  public static void onAlarm() {
    UpdateService.start(false);
    schedule();
  }

  public static void onTimeChanged() {
    schedule();
  }
}