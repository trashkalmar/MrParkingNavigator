package ru.mail.parking.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import ru.mail.parking.App;
import ru.mail.parking.R;
import ru.mail.parking.floors.Place;
import ru.mail.parking.utils.Utils;
import ru.mail.parking.widget.MainWidgetProvider;

import static ru.mail.parking.App.floors;

public class NavigatorActivity extends Activity {

  private Place mPlace;
  private boolean mFromInput;


  public void onCreate(Bundle state) {
    super.onCreate(state);
    requestWindowFeature(Window.FEATURE_NO_TITLE);

    Bundle extras = state;
    if (extras == null)
      extras = getIntent().getExtras();

    if (extras != null) {
      mPlace = floors().getPlace(extras.getInt(Utils.EXTRA_PLACE, 0));
      mFromInput = extras.getBoolean(EnterPlaceActivity.EXTRA_FROM_INPUT, false);
    }

    if (mPlace == null)
      mPlace = App.prefs().getStoredPlace();

    if (mPlace == null) {
      // Something bad happened!
      finish();
      return;
    }

    setContentView(R.layout.navigator);

    MultiscrollView scrollView = (MultiscrollView)findViewById(R.id.scroll);
    NavigatorFloorView floorView = new NavigatorFloorView(mPlace, scrollView);
    scrollView.setAttachedView(floorView);

    TextView ok = (TextView)findViewById(R.id.ok);
    if (mFromInput) {
      ok.setText(R.string.floor_ok);
      ok.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          App.prefs().setStoredPlace(mPlace.getNumber());
          MainWidgetProvider.updateAll();
          finish();
        }
      });
    } else {
      ok.setText(R.string.floor_leave);
      ok.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          App.prefs().setStoredPlace(Place.INVALID);
          MainWidgetProvider.updateAll();
          finish();
        }
      });
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle state) {
    super.onSaveInstanceState(state);
    state.putInt(Utils.EXTRA_PLACE, mPlace.getNumber());
    state.putBoolean(EnterPlaceActivity.EXTRA_FROM_INPUT, mFromInput);
  }

  @Override
  public void onBackPressed() {
    if (mFromInput)
      startActivity(new Intent(this, EnterPlaceActivity.class)
                       .putExtra(Utils.EXTRA_PLACE, mPlace.getNumber()));

    super.onBackPressed();
  }
}