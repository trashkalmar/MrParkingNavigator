package ru.mail.parking.sw2.screens;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.extension.util.control.ControlView;
import com.sonyericsson.extras.liveware.extension.util.control.ControlViewGroup;

import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import ru.mail.parking.App;
import ru.mail.parking.R;
import ru.mail.parking.floors.Place;
import ru.mail.parking.sw2.SwControlFlow;
import ru.mail.parking.widget.MainWidgetProvider;

import static ru.mail.parking.App.app;

public class SwEnterPlaceScreen extends SwBaseScreen {
  private int mPlace;
  private int mPlaceId;
  private int mInfoId;
  private int mBackspaceId;
  private int mOkId;

  private final List<Bundle> mLayoutData = new ArrayList<>();


  private final ControlView.OnClickListener mNumClickListener = new ControlView.OnClickListener() {
    @Override
    public void onClick(ControlView v) {
      if (mPlace > 99)
        mPlace = 0;

      int num = Integer.valueOf((String)v.getTag());
      mPlace = (mPlace * 10) + num;

      update();
    }
  };


  private void enumChildren(ControlViewGroup root, List<Bundle> data) {
    SparseArray<ControlView> views = root.getViews();

    for (int i = 0; i < views.size(); i++) {
      ControlView v = views.valueAt(i);

      if (v.getTag() instanceof String) {
        String tag = (String)v.getTag();
        if (tag.startsWith("b:")) {
          tag = tag.substring(2);
          v.setTag(tag);

          Bundle b = new Bundle(2);
          b.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, v.getId());
          b.putString(Control.Intents.EXTRA_TEXT, tag);
          data.add(b);

          v.setOnClickListener(mNumClickListener);
        }
      }
    }
  }

  private void update() {
    boolean empty = (mPlace == 0);
    String place = (empty ? "" : String.valueOf(mPlace));
    mFlow.sendText(mPlaceId, place);

    String backspace = "<";
    String info = "";
    boolean valid;
    if (empty) {
      valid = false;
      backspace = "";
    } else {
      Place p = App.floors().getPlace(mPlace);
      if (p == null)
        info = app().getString(R.string.floor_invalid);
      else
        info = app().getString(R.string.sw_floor_format, p.getFloor(), p.getSide().name());

      valid = (p != null);
    }

    mFlow.sendText(mInfoId, info);
    mFlow.sendText(mBackspaceId, backspace);

    String ok = (valid ? App.app().getString(R.string.floor_ok) : "");
    mFlow.sendText(mOkId, ok);
  }

  public SwEnterPlaceScreen(SwControlFlow flow, int place) {
    super(flow);
    mPlace = (place == Place.INVALID ? 0 : place);
  }

  @Override
  protected void setupLayout(ControlViewGroup root, View view) {
    super.setupLayout(root, view);

    mPlaceId = root.findViewById(R.id.place).getId();
    mInfoId = root.findViewById(R.id.info).getId();

    ControlView v = root.findViewById(R.id.backspace);
    mBackspaceId = v.getId();
    v.setOnClickListener(new ControlView.OnClickListener() {
      @Override
      public void onClick(ControlView v) {
        if (mPlace == 0)
          return;

        mPlace /= 10;
        update();
      }
    });

    v = root.findViewById(R.id.ok);
    mOkId = v.getId();
    v.setOnClickListener(new ControlView.OnClickListener() {
      @Override
      public void onClick(ControlView v) {
        Place p = App.floors().getPlace(mPlace);
        if (p != null) {
          App.prefs().setStoredPlace(mPlace);
          mFlow.run(new SwPlaceScreen(mFlow, true));

          MainWidgetProvider.updateAll();
        }
      }
    });

    enumChildren(root, mLayoutData);
  }

  @Override
  public void onResume() {
    update();
  }

  @Override
  protected int getLayoutResource() {
    return R.layout.sw_enter_place;
  }

  @Override
  protected List<Bundle> getLayoutData() {
    return mLayoutData;
  }
}