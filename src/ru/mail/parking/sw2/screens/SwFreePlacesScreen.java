package ru.mail.parking.sw2.screens;

import com.sonyericsson.extras.liveware.extension.util.control.ControlView;
import com.sonyericsson.extras.liveware.extension.util.control.ControlViewGroup;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import ru.mail.parking.Preferences;
import ru.mail.parking.R;
import ru.mail.parking.floors.Place;
import ru.mail.parking.sw2.SwControlFlow;
import ru.mail.parking.widget.SmartUpdate;

import static ru.mail.parking.App.prefs;

public class SwFreePlacesScreen extends SwBaseScreen {
  private int mCountId;
  private int mInfoId;

  private final SharedPreferences.OnSharedPreferenceChangeListener mListener =
    new SharedPreferences.OnSharedPreferenceChangeListener() {
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
      if (Preferences.Keys.last_fetch.name().equals(key))
        update();
    }
  };


  public SwFreePlacesScreen(SwControlFlow flow) {
    super(flow);
  }

  @Override
  protected int getLayoutResource() {
    return R.layout.sw_free;
  }

  private void update() {
    int free = prefs().getLastPlaces();
    mFlow.sendText(mCountId, (free == Place.INVALID ? "???" : String.valueOf(free)));
    mFlow.sendText(mInfoId, prefs().getSwLastRefresh());
  }

  @Override
  protected List<Bundle> getLayoutData() {
    List<Bundle> data = new ArrayList<>();
    setButtons(data, R.string.sw_set_place, R.string.sw_refresh);
    return data;
  }

  @Override
  protected void setupLayout(ControlViewGroup root, View view) {
    super.setupLayout(root, view);

    root.findViewById(R.id.left).setOnClickListener(new ControlView.OnClickListener() {
      @Override
      public void onClick(ControlView v) {
        mFlow.run(new SwEnterPlaceScreen(mFlow, Place.INVALID));
      }
    });

    root.findViewById(R.id.right).setOnClickListener(new ControlView.OnClickListener() {
      @Override
      public void onClick(ControlView v) {
        SmartUpdate.force();
      }
    });

    mCountId = root.findViewById(R.id.count).getId();
    mInfoId = root.findViewById(R.id.info).getId();
  }

  @Override
  public void onResume() {
    super.onResume();
    prefs().registerListener(mListener);
    update();
  }

  @Override
  public void onPause() {
    super.onPause();
    prefs().unregisterListener(mListener);
  }
}