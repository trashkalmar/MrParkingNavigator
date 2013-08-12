package ru.mail.parking.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import ru.mail.parking.widget.utils.NetworkAwaiter;

import static ru.mail.parking.widget.App.app;

public final class SmartUpdate {
  private static AlarmManager mManager;

  static {
    mManager = (AlarmManager)app().getSystemService(Context.ALARM_SERVICE);
  }

  private SmartUpdate() {}


  public enum Policy {
    auto {
      @Override
      public void schedule() {
        //schedule(60 * 60000);
        schedule(6000);
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
        sDefault = valueOf(app().getString(R.string.prefs_config_update_mode_default));

      return sDefault;
    }

    protected void schedule(long delay) {
      mManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + delay, createActionIntent());
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
      }
    });
  }

  private static void schedule() {
    App.prefs().getUpdatePolicy().schedule();
  }

  public static void force() {
    abort();
    execute(true);
    schedule();
  }

  public static void abort() {
    PendingIntent pi = createActionIntent();
    mManager.cancel(pi);
    pi.cancel();

    NetworkAwaiter.getInstance().cancelAll();
  }

  public static void restart() {
    abort();
    schedule();
  }

  public static void onAlarm() {
    execute(false);
    schedule();
  }

  public static void onTimeChanged() {
    restart();
  }
}