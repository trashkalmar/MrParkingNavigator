package ru.mail.parking.widget;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {
  private final SharedPreferences mPrefs;

  private enum Keys {
    last_fetch,
    last_update,
    last_places,
    click_action,
    time_format,
    update_policy
  }

  public enum ClickAction {
    update,
    settings;

    public static ClickAction getDefault() {
      return update;
    }
  }

  public enum UpdatePolicy {
    auto,
    manual,
    smart;

    public static UpdatePolicy getDefault() {
      return smart;
    }
  }

  public enum TimeFormat {
    none {
      @Override
      public String getFormatString() {
        return "";
      }
    },
    time {
      @Override
      public String getFormatString() {
        return App.app().getString(R.string.time_format);
      }
    },
    full {
      @Override
      public String getFormatString() {
        return time.getFormatString() + " " + App.app().getString(R.string.time_format_date);
      }
    };

    public static TimeFormat getDefault() {
      return full;
    }

    public abstract String getFormatString();
  }


  public Preferences() {
    mPrefs = App.app().getSharedPreferences("settings", Context.MODE_PRIVATE);
  }

  public String getLastFetch() {
    return App.formatDate(mPrefs.getLong(Keys.last_fetch.name(), 0), TimeFormat.full, false);
  }

  public String getLastUpdate() {
    return App.formatDate(mPrefs.getLong(Keys.last_update.name(), 0), getTimeFormat(), true);
  }

  public int getLastPlaces() {
    return mPrefs.getInt(Keys.last_places.name(), -1);
  }

  public ClickAction getClickAction() {
    String v = mPrefs.getString(Keys.click_action.name(), ClickAction.getDefault().name());
    try {
      return ClickAction.valueOf(v);
    } catch (IllegalArgumentException e) {
      return ClickAction.getDefault();
    }
  }

  public UpdatePolicy getUpdatePolicy() {
    String v = mPrefs.getString(Keys.update_policy.name(), UpdatePolicy.getDefault().name());
    try {
      return UpdatePolicy.valueOf(v);
    } catch (IllegalArgumentException e) {
      return UpdatePolicy.getDefault();
    }
  }

  public TimeFormat getTimeFormat() {
    String v = mPrefs.getString(Keys.time_format.name(), TimeFormat.getDefault().name());
    try {
      return TimeFormat.valueOf(v);
    } catch (IllegalArgumentException e) {
      return TimeFormat.getDefault();
    }
  }

  public void setLastInfo(int places, long lastUpdate) {
    mPrefs.edit()
          .putInt(Keys.last_places.name(), places)
          .putLong(Keys.last_update.name(), lastUpdate)
          .putLong(Keys.last_fetch.name(), System.currentTimeMillis())
          .commit();
  }

  public void setClickAction(ClickAction action) {
    mPrefs.edit()
          .putString(Keys.click_action.name(), action.name())
          .commit();
  }

  public void setUpdatePolicy(UpdatePolicy policy) {
    mPrefs.edit()
          .putString(Keys.update_policy.name(), policy.name())
          .commit();
  }

  public void setTimeFormat(TimeFormat format) {
    mPrefs.edit()
          .putString(Keys.time_format.name(), format.name())
          .commit();
  }
}