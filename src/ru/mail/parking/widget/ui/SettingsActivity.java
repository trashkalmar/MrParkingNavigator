package ru.mail.parking.widget.ui;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import ru.mail.parking.widget.R;

public class SettingsActivity extends PreferenceActivity {
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    addPreferencesFromResource(R.xml.settings);
  }
}