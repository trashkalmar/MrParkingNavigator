package ru.mail.parking.sw2.screens;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.extension.util.control.ControlObjectClickEvent;
import com.sonyericsson.extras.liveware.extension.util.control.ControlView;
import com.sonyericsson.extras.liveware.extension.util.control.ControlViewGroup;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import java.util.Collections;
import java.util.List;

import ru.mail.parking.R;
import ru.mail.parking.sw2.SwControlFlow;

import static ru.mail.parking.App.app;

public abstract class SwBaseScreen {
  protected SwControlFlow mFlow;
  protected ControlViewGroup mRoot;
  protected boolean mAbort;

  public SwBaseScreen(SwControlFlow flow) {
    mFlow = flow;
  }

  public void onResume() {}

  public void onPause() {}

  public boolean start() {
    int layout = getLayoutResource();
    LayoutInflater inflater = (LayoutInflater)app().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View view = inflater.inflate(layout, null);
    ControlViewGroup root = mFlow.parseLayout(view);
    setupLayout(root, view);

    if (mAbort)
      return false;

    List<Bundle> data = getLayoutData();
    if (mAbort)
      return false;

    Bundle[] da = null;
    if (!data.isEmpty()) {
      da = new Bundle[data.size()];
      //noinspection SuspiciousSystemArraycopy
      System.arraycopy(data.toArray(), 0, da, 0, data.size());
    }

    if (!mAbort)
      mFlow.showLayout(layout, da);

    return !mAbort;
  }

  public void onObjectClick(ControlObjectClickEvent event) {
    ControlView v = mRoot.findViewById(event.getLayoutReference());
    if (v != null)
      v.onClick();
  }

  protected void setupLayout(ControlViewGroup root, View view) {
    mRoot = root;
  }

  protected List<Bundle> getLayoutData() {
    return Collections.emptyList();
  }

  protected void setButtons(List<Bundle> data, int leftText, int rightText) {
    Bundle b = new Bundle(2);
    b.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.left);
    b.putString(Control.Intents.EXTRA_TEXT, app().getString(leftText));
    data.add(b);

    b = new Bundle(2);
    b.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.right);
    b.putString(Control.Intents.EXTRA_TEXT, app().getString(rightText));
    data.add(b);
  }

  public boolean onBack() {
    return false;
  }

  protected abstract int getLayoutResource();
}