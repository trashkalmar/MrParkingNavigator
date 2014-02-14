package ru.mail.parking.sw2.system;

import com.sonyericsson.extras.liveware.extension.util.ExtensionService;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.registration.DeviceInfoHelper;
import com.sonyericsson.extras.liveware.extension.util.registration.RegistrationInformation;

import ru.mail.parking.App;
import ru.mail.parking.sw2.SwControlFlow;

public class SwService extends ExtensionService {
  private static final SwRegInfo REG_INFO = new SwRegInfo();

  public SwService() {
    super(SwRegInfo.EXTENSION_KEY);
  }

  @Override
  protected RegistrationInformation getRegistrationInformation() {
    return REG_INFO;
  }

  @Override
  protected boolean keepRunningWhenConnected() {
    return false;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    App.app().setHasSmartwatch(areAnyAccessoriesConnected());
  }

  @Override
  protected void onConnectionChanged(boolean connected) {
    App.app().setHasSmartwatch(connected);
  }

  @Override
  public ControlExtension createControlExtension(String hostAppPackageName) {
    if (!DeviceInfoHelper.isSmartWatch2ApiAndScreenDetected(this, hostAppPackageName))
      throw new IllegalArgumentException("SmartWatch 2 not found");

    return new SwControlFlow(hostAppPackageName);
  }
}