package ru.mail.parking.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import ru.mail.parking.App;
import ru.mail.parking.R;
import ru.mail.parking.floors.Place;
import ru.mail.parking.utils.Utils;

import static ru.mail.parking.App.app;
import static ru.mail.parking.App.prefs;

public class EnterPlaceActivity extends Activity {
  public static final String EXTRA_FROM_INPUT = "from input";

  private int mPlace;

  private TextView mPlaceView;
  private TextView mInfoView;
  private TextView mOk;
  private TextView mButton0;


  private final View.OnClickListener mNumClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      if (mPlace > 99)
        mPlace = 0;

      int num = Integer.valueOf((String)v.getTag());
      mPlace = (mPlace * 10) + num;

      update();
    }
  };

  private final View.OnClickListener mSettingsClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      startActivity(new Intent(EnterPlaceActivity.this, SettingsActivity.class));
    }
  };


  private void enumChildren(View parent) {
    if (!(parent instanceof ViewGroup))
      return;

    ViewGroup vg = (ViewGroup)parent;

    int count = vg.getChildCount();
    for (int i = 0; i < count; i++) {
      View v = vg.getChildAt(i);
      if (v instanceof ViewGroup) {
        enumChildren(v);
        continue;
      }

      if (v.getTag() instanceof String) {
        String tag = (String)v.getTag();
        if (tag.startsWith("b:")) {
          tag = tag.substring(2);
          v.setTag(tag);
          ((TextView)v).setText(tag);
          v.setOnClickListener(mNumClickListener);

          if (tag.equals("0"))
            mButton0 = (TextView)v;
        }
      }
    }
  }

  public void onCreate(Bundle state) {
    super.onCreate(state);

    if (prefs().getStoredPlace() != null) {
      startActivity(new Intent(this, NavigatorActivity.class));
      finish();
      return;
    }

    Bundle extras = state;
    if (extras == null)
      extras = getIntent().getExtras();

    if (extras != null)
      mPlace = extras.getInt(Utils.EXTRA_PLACE, 0);

    setContentView(R.layout.enter_place);

    mPlaceView = (TextView)findViewById(R.id.place);
    mInfoView = (TextView)findViewById(R.id.info);

    TextView backspace = (TextView)findViewById(R.id.backspace);
    backspace.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (mPlace == 0)
          return;

        mPlace /= 10;
        update();
      }
    });

    mOk = (TextView)findViewById(R.id.ok);
    mOk.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Place p = App.floors().getPlace(mPlace);
        if (p == null)
          return;

        startActivity(new Intent(EnterPlaceActivity.this, NavigatorActivity.class)
                         .putExtra(Utils.EXTRA_PLACE, mPlace)
                         .putExtra(EXTRA_FROM_INPUT, true));
        finish();
      }
    });

    enumChildren(findViewById(R.id.buttons));
    update();
  }

  @Override
  protected void onSaveInstanceState(Bundle state) {
    super.onSaveInstanceState(state);
    state.putInt(Utils.EXTRA_PLACE, mPlace);
  }

  private void update() {
    boolean empty = (mPlace == 0);
    String place = (empty ? "" : String.valueOf(mPlace));
    mPlaceView.setText(place);

    int weight = 1;
    String info = "";
    if (empty) {
      mOk.setVisibility(View.GONE);
      weight = 2;

      mButton0.setText(R.string.floor_settings);
      mButton0.setOnClickListener(mSettingsClickListener);
    } else {
      mOk.setVisibility(View.VISIBLE);

      mButton0.setText("0");
      mButton0.setOnClickListener(mNumClickListener);

      Place p = App.floors().getPlace(mPlace);
      if (p == null)
        info = app().getString(R.string.floor_invalid);
      else
        info = app().getString(R.string.floor_format, p.getFloor(), p.getSide().name());
    }

    TableRow.LayoutParams lp = (TableRow.LayoutParams)mButton0.getLayoutParams();
    lp.weight = weight;
    mButton0.setLayoutParams(lp);
    mInfoView.setText(info);
  }
}