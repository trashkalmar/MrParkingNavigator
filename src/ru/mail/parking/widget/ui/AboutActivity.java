package ru.mail.parking.widget.ui;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import ru.mail.parking.widget.R;

public class AboutActivity extends Activity {
  public void onCreate(Bundle state) {
    super.onCreate(state);
    setContentView(R.layout.about);

    try {
      PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
      ((TextView)findViewById(R.id.version))
        .setText(getString(R.string.about_version, info.versionName + " (" + getString(R.string.build_date) + ")"));
    } catch (PackageManager.NameNotFoundException e) {
      findViewById(R.id.version).setVisibility(View.GONE);
    }
  }
}