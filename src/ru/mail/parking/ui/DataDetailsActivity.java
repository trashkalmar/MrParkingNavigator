package ru.mail.parking.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import ru.mail.parking.App;
import ru.mail.parking.R;

public class DataDetailsActivity extends Activity {
  public void onCreate(Bundle state) {
    super.onCreate(state);

    setContentView(R.layout.data_details);
    ((TextView)findViewById(R.id.fetched))
      .setText(getString(R.string.info_last_fetch, App.prefs().getLastFetch()));

    ((TextView)findViewById(R.id.updated))
      .setText(getString(R.string.info_last_refresh, App.prefs().getLastRefresh()));

    ((TextView)findViewById(R.id.places))
      .setText(getString(R.string.info_last_places, App.prefs().getLastPlaces()));

    ((TextView)findViewById(R.id.data))
      .setText(App.prefs().getLastData());
  }
}