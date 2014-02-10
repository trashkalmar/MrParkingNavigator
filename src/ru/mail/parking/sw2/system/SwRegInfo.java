package ru.mail.parking.sw2.system;

import com.sonyericsson.extras.liveware.aef.registration.Registration;
import com.sonyericsson.extras.liveware.extension.util.ExtensionUtils;
import com.sonyericsson.extras.liveware.extension.util.registration.RegistrationInformation;

import android.content.ContentValues;

import ru.mail.parking.R;
import ru.mail.parking.ui.SettingsActivity;

import static ru.mail.parking.App.app;

public class SwRegInfo extends RegistrationInformation {
  public static final String EXTENSION_KEY = app().getPackageName();
  private static final ContentValues INFO = new ContentValues();

  public SwRegInfo() {
    INFO.put(Registration.ExtensionColumns.CONFIGURATION_ACTIVITY, SettingsActivity.class.getName());
    INFO.put(Registration.ExtensionColumns.NAME, app().getString(R.string.sw_title));
    INFO.put(Registration.ExtensionColumns.EXTENSION_KEY, EXTENSION_KEY);
    INFO.put(Registration.ExtensionColumns.LAUNCH_MODE, Registration.LaunchMode.CONTROL);

    String icon = ExtensionUtils.getUriString(app(), R.drawable.icon);
    INFO.put(Registration.ExtensionColumns.HOST_APP_ICON_URI, icon);

    icon = ExtensionUtils.getUriString(app(), R.drawable.icon_sw);
    INFO.put(Registration.ExtensionColumns.EXTENSION_48PX_ICON_URI, icon);
  }

  @Override
  public ContentValues getExtensionRegistrationConfiguration() {
    return INFO;
  }

  @Override
  public int getRequiredWidgetApiVersion() {
    return RegistrationInformation.API_NOT_REQUIRED;
  }

  @Override
  public int getRequiredSensorApiVersion() {
    return RegistrationInformation.API_NOT_REQUIRED;
  }

  @Override
  public int getRequiredNotificationApiVersion() {
    return RegistrationInformation.API_NOT_REQUIRED;
  }

  @Override
  public int getRequiredControlApiVersion() {
    return 2;
  }

  @Override
  public boolean controlInterceptsBackButton() {
    return true;
  }

  @Override
  public boolean isDisplaySizeSupported(int width, int height) {
    return width == app().getResources().getDimensionPixelSize(R.dimen.smart_watch_2_control_width) &&
           height == app().getResources().getDimensionPixelSize(R.dimen.smart_watch_2_control_height);
  }
}