package ru.mail.parking.sw2;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.control.ControlObjectClickEvent;
import com.sonyericsson.extras.liveware.extension.util.control.ControlViewGroup;

import android.os.Bundle;
import android.view.View;

import ru.mail.parking.App;
import ru.mail.parking.sw2.screens.SwBaseScreen;
import ru.mail.parking.sw2.screens.SwFreePlacesScreen;
import ru.mail.parking.sw2.screens.SwPlaceScreen;

public class SwControlFlow extends ControlExtension {
  private SwBaseScreen mCurScreen;


  public SwControlFlow(String hostAppPackageName) {
    super(App.app(), hostAppPackageName);
    run(App.prefs().getStoredPlace() == null ? new SwFreePlacesScreen(this)
                                             : new SwPlaceScreen(this, false));
  }

  @Override
  public void onObjectClick(ControlObjectClickEvent event) {
    mCurScreen.onObjectClick(event);
  }

  public void run(SwBaseScreen screen) {
    if (screen.start()) {
      if (mCurScreen != null)
        mCurScreen.onPause();

      mCurScreen = screen;
      mCurScreen.onResume();
    }
  }

  @Override
  public ControlViewGroup parseLayout(View v) {
    return super.parseLayout(v);
  }

  @Override
  public void showLayout(int layoutId, Bundle[] layoutData) {
    super.showLayout(layoutId, layoutData);
  }

  @Override
  public void sendText(int layoutReference, String text) {
    super.sendText(layoutReference, text);
  }

  @Override
  public void onKey(int action, int keyCode, long timeStamp) {
    if (keyCode != Control.KeyCodes.KEYCODE_BACK)
      return;

    if (!mCurScreen.onBack())
      goHome();
  }

  @Override
  public void onPause() {
    if (mCurScreen != null)
      mCurScreen.onPause();
  }

  @Override
  public void onResume() {
    if (mCurScreen != null)
      mCurScreen.onResume();
  }

  public void goHome() {
    stopRequest();
  }
}