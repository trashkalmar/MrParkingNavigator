package ru.mail.parking.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import java.util.Calendar;

import ru.mail.parking.App;
import ru.mail.parking.R;
import ru.mail.parking.utils.NetworkAwaiter;

import static java.util.Calendar.DAY_OF_WEEK;
import static java.util.Calendar.DAY_OF_YEAR;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.SATURDAY;
import static java.util.Calendar.SECOND;
import static java.util.Calendar.SUNDAY;
import static java.util.Calendar.getInstance;
import static ru.mail.parking.App.app;

public final class SmartUpdate {
  private static final AlarmManager sAlarmManager;

  static {
    sAlarmManager = (AlarmManager)app().getSystemService(Context.ALARM_SERVICE);
  }

  private SmartUpdate() {}


  @SuppressWarnings("UnusedDeclaration")
  public enum Policy {
    auto {
      @Override
      public void schedule() {
        schedule(REGULAR_INTERVAL);
      }
    },

    manual {
      @Override
      public void schedule() {
        // Do nothing here
      }
    },

    smart {
      private static final int WORKDAY_HOT_START = 9;              // Hour at which the `hot` period starts…
      private static final int WORKDAY_HOT_END = 12;               // …and ends
      private static final int WORKDAY_HOT_INTERVAL = 15 * 60000;  // Update interval within `hot` period
      private static final int WORKDAY_INTERVAL = REGULAR_INTERVAL;

      private Calendar newZero(Calendar c) {
        Calendar res = (Calendar)c.clone();
        res.set(HOUR_OF_DAY, 0);
        res.set(MINUTE, 0);
        res.set(SECOND, 0);

        return res;
      }

      @Override
      public void schedule() {
        final Calendar now = getInstance();
        Calendar next = newZero(now);
        int weekday = now.get(DAY_OF_WEEK);
        int hour = now.get(HOUR_OF_DAY);

        // Holiday?
        if (weekday == SATURDAY || weekday == SUNDAY) {
          next.add(DAY_OF_YEAR, 1);
          if (next.get(DAY_OF_WEEK) == SUNDAY)
            next.add(DAY_OF_YEAR, 1);

          next.add(HOUR_OF_DAY, WORKDAY_HOT_START);
          schedule(next.getTimeInMillis() - now.getTimeInMillis());
          return;
        }

        // Before `hot` time?
        if (hour < WORKDAY_HOT_START) {
          next.set(HOUR_OF_DAY, WORKDAY_HOT_START);
          schedule(next.getTimeInMillis() - now.getTimeInMillis());
          return;
        }

        // Within `hot` time?
        if (hour < WORKDAY_HOT_END) {
          schedule(WORKDAY_HOT_INTERVAL);
          return;
        }

        // Past `hot time?`
        schedule(WORKDAY_INTERVAL);
      }
    };


    private static final int REGULAR_INTERVAL = 60 * 60000;

    private static Policy sDefault;

    public static Policy getDefault() {
      if (sDefault == null)
        sDefault = valueOf(app().getString(R.string.prefs_config_update_mode_default));

      return sDefault;
    }

    protected void schedule(long delay) {
      sAlarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + delay, createActionIntent());
    }

    public abstract void schedule();
  }


  private static PendingIntent createActionIntent() {
    Intent it = new Intent(app(), MainReceiver.class)
                   .setAction(MainReceiver.ACTION_UPDATE);

    return PendingIntent.getBroadcast(app(), 0, it, PendingIntent.FLAG_CANCEL_CURRENT);
  }

  private static void execute(final boolean force) {
    NetworkAwaiter.getInstance().start(SmartUpdate.class.getSimpleName(), new Runnable() {
      @Override
      public void run() {
        UpdateService.start(force);
        schedule();
      }
    });
  }

  private static void schedule() {
    App.prefs().getUpdatePolicy().schedule();
  }

  public static void force() {
    abort();
    execute(true);
  }

  public static void abort() {
    PendingIntent pi = createActionIntent();
    sAlarmManager.cancel(pi);
    pi.cancel();

    NetworkAwaiter.getInstance().cancelAll();
  }

  public static void restart() {
    abort();
    schedule();
  }

  public static void onAlarm() {
    execute(false);
  }

  public static void onTimeChanged() {
    restart();
  }
}