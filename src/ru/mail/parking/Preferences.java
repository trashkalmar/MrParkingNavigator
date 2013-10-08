package ru.mail.parking;

import android.content.Context;
import android.content.SharedPreferences;

import ru.mail.parking.floors.Place;
import ru.mail.parking.utils.Utils;
import ru.mail.parking.widget.MainWidgetProvider;
import ru.mail.parking.widget.SmartUpdate;

public class Preferences implements SharedPreferences.OnSharedPreferenceChangeListener {
  public static final String NAME = "settings";

  private final SharedPreferences mPrefs;

  public enum Keys {
    last_fetch,
    last_update,
    last_places,
    last_data,
    click_action,
    doubletap_action,
    time_format,
    update_policy,
    stored_place
  }

  public enum ClickAction {
    navigator,
    update,
    details,
    settings;

    private static ClickAction sDefault;
    private static ClickAction sDefaultDoubletap;

    static ClickAction getDefault(boolean doubletap) {
      if (sDefault == null) {
        sDefault = valueOf(App.app().getString(R.string.prefs_config_click_action_default));
        sDefaultDoubletap = valueOf(App.app().getString(R.string.prefs_config_doubletap_action_default));
      }

      return (doubletap ? sDefaultDoubletap : sDefault);
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
      SmartUpdate.restart();
  }

  public String getLastFetch() {
    return Utils.formatDate(mPrefs.getLong(Keys.last_fetch.name(), 0), TimeFormat.full, false);
  }

  public String getLastRefresh() {
    return Utils.formatDate(mPrefs.getLong(Keys.last_update.name(), 0), getTimeFormat(), true);
  }

  public int getLastPlaces() {
    return mPrefs.getInt(Keys.last_places.name(), -1);
  }

  public String getLastData() {
    return mPrefs.getString(Keys.last_data.name(), App.app().getString(R.string.unknown));
  }

  public Place getStoredPlace() {
    int p = mPrefs.getInt(Keys.stored_place.name(), Place.INVALID);
    return App.floors().getPlace(p);
  }

  public ClickAction getClickAction() {
    String v = mPrefs.getString(Keys.click_action.name(), ClickAction.getDefault(false).name());
    try {
      return ClickAction.valueOf(v);
    } catch (IllegalArgumentException e) {
      return ClickAction.getDefault(false);
    }
  }

  public ClickAction getDoubletapAction() {
    String v = mPrefs.getString(Keys.doubletap_action.name(), ClickAction.getDefault(true).name());
    try {
      return ClickAction.valueOf(v);
    } catch (IllegalArgumentException e) {
      return ClickAction.getDefault(true);
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
          .putLong(Keys.last_fetch.name(), System.currentTimeMillis())
          .putString(Keys.last_data.name(), data)
          .commit();
  }

  public void setStoredPlace(int place) {
    SharedPreferences.Editor e = mPrefs.edit();
    if (place == Place.INVALID) e.remove(Keys.stored_place.name());
                           else e.putInt(Keys.stored_place.name(), place);
    e.commit();
  }
}