package ru.mail.parking.widget;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences implements SharedPreferences.OnSharedPreferenceChangeListener {
  public static final String NAME = "settings";

  private final SharedPreferences mPrefs;

  public enum Keys {
    last_fetch,
    last_update,
    last_places,
    last_data,
    click_action,
    time_format,
    update_policy
  }

  public enum ClickAction {
    update,
    details;

    private static ClickAction sDefault;

    public static ClickAction getDefault() {
      if (sDefault == null)
        sDefault = valueOf(App.app().getString(R.string.prefs_config_click_action_default));

      return sDefault;
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

    private static TimeFormat sDefault;

    public static TimeFormat getDefault() {
      if (sDefault == null)
        sDefault = valueOf(App.app().getString(R.string.prefs_config_time_format_default));

      return sDefault;
    }

    public abstract String getFormatString();
  }


  public Preferences() {
    mPrefs = App.app().getSharedPreferences(NAME, Context.MODE_PRIVATE);
    mPrefs.registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (Keys.time_format.name().equals(key))
      MainWidgetProvider.updateAll();

    if (Keys.update_policy.name().equals(key))
      SmartUpdate.schedule();
  }

  public String getLastFetch() {
    return App.formatDate(mPrefs.getLong(Keys.last_fetch.name(),0),TimeFormat.full,false);
  }

  public String getLastRefresh() {
    return App.formatDate(mPrefs.getLong(Keys.last_update.name(), 0), getTimeFormat(), true);
  }

  public int getLastPlaces() {
    return mPrefs.getInt(Keys.last_places.name(), -1);
  }

  public String getLastData() {
    return mPrefs.getString(Keys.last_data.name(), App.app().getString(R.string.unknown));
  }

  public ClickAction getClickAction() {
    String v = mPrefs.getString(Keys.click_action.name(), ClickAction.getDefault().name());
    try {
      return ClickAction.valueOf(v);
    } catch (IllegalArgumentException e) {
      return ClickAction.getDefault();
    }
  }

  public SmartUpdate.Policy getUpdatePolicy() {
    String v = mPrefs.getString(Keys.update_policy.name(), SmartUpdate.Policy.getDefault().name());
    try {
      return SmartUpdate.Policy.valueOf(v);
    } catch (IllegalArgumentException e) {
      return SmartUpdate.Policy.getDefault();
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

  public void setLastInfo(int places, long lastUpdate, String data) {
    mPrefs.edit()
          .putInt(Keys.last_places.name(), places)
          .putLong(Keys.last_update.name(), lastUpdate)
          .putLong(Keys.last_fetch.name(),System.currentTimeMillis())
          .putString(Keys.last_data.name(), data)
          .commit();
  }
}