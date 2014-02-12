package ru.mail.parking.sw2.screens;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.extension.util.control.ControlView;
import com.sonyericsson.extras.liveware.extension.util.control.ControlViewGroup;

import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import ru.mail.parking.R;
import ru.mail.parking.floors.Place;
import ru.mail.parking.sw2.SwControlFlow;
import ru.mail.parking.widget.MainWidgetProvider;

import static ru.mail.parking.App.app;
import static ru.mail.parking.App.prefs;

public class SwPlaceScreen extends SwBaseScreen {
  private final boolean mFromInput;
  private final Place mPlace;


  public SwPlaceScreen(SwControlFlow flow, boolean fromInput) {
    super(flow);
    mFromInput = fromInput;
    mPlace = prefs().getStoredPlace();
  }

  @Override
  protected int getLayoutResource() {
    return R.layout.sw_place;
  }

  @Override
  protected List<Bundle> getLayoutData() {
    if (mPlace == null) {
      mFlow.run(new SwEnterPlaceScreen(mFlow, Place.INVALID));
      mAbort = true;
      return super.getLayoutData();
    }

    List<Bundle> data = new ArrayList<>();
    setButtons(data, R.string.sw_leave, R.string.sw_show);

    Bundle b = new Bundle(2);
    b.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.count);
    b.putString(Control.Intents.EXTRA_TEXT, String.valueOf(mPlace.getNumber()));
    data.add(b);

    b = new Bundle(2);
    b.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.info);
    b.putString(Control.Intents.EXTRA_TEXT, app().getString(R.string.sw_floor_format,
                                                            mPlace.getFloor(), mPlace.getSide().name()));
    data.add(b);

    return data;
  }

  @Override
  public boolean onBack() {
    if (mFromInput) {
      mFlow.run(new SwEnterPlaceScreen(mFlow, mPlace.getNumber()));
      return true;
    }

    return false;
  }

  @Override
  protected void setupLayout(ControlViewGroup root, View view) {
    super.setupLayout(root, view);

    root.findViewById(R.id.left).setOnClickListener(new ControlView.OnClickListener() {
      @Override
      public void onClick(ControlView v) {
        prefs().setStoredPlace(Place.INVALID);
        MainWidgetProvider.updateAll();

        mFlow.goHome();
      }
    });

    root.findViewById(R.id.right).setOnClickListener(new ControlView.OnClickListener() {
      @Override
      public void onClick(ControlView v) {
        mFlow.run(new SwNavigatorScreen(mFlow));
      }
    });
  }
}