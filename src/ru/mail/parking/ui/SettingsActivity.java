package ru.mail.parking.ui;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import ru.mail.parking.Preferences;
import ru.mail.parking.R;

public class SettingsActivity extends PreferenceActivity {
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    getPreferenceManager().setSharedPreferencesName(Preferences.NAME);
    addPreferencesFromResource(R.xml.settings);
  }
}