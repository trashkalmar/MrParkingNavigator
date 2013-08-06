package ru.mail.parking.widget;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Intent;
import android.os.PowerManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UpdateService extends IntentService {
  private static final String SERVICE_NAME = "Mail.Ru Parking Monitor update service";

  private static final String DATA_URL = "http://p.corp.mail.ru/api/json";
  private static final String DATA_PLACES = "places";
  private static final String DATA_TIME = "time";
  private static final SimpleDateFormat DATA_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

  private static volatile boolean sIsUpdating = false;


  public UpdateService() {
    super(SERVICE_NAME);
  }

  private void updateData() {
    sIsUpdating = true;
    PowerManager.WakeLock wl = ((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, SERVICE_NAME);

    try {
      wl.acquire();

      URL url = new URL(DATA_URL);
      HttpURLConnection cn = (HttpURLConnection)url.openConnection();
      cn.setDoInput(true);

      InputStream is = null;
      String line;
      try {
        is = cn.getInputStream();
        BufferedReader buffRead = new BufferedReader(new InputStreamReader(is));
        line = buffRead.readLine();
      } finally {
        cn.disconnect();

        if (is != null)
          try {
            is.close();
          } catch (IOException ignored) {}
      }

      JSONObject jo = new JSONObject(line);
      int free = jo.getInt(DATA_PLACES);
      String ts = jo.getString(DATA_TIME);
      Date d = DATA_TIME_FORMAT.parse(ts);

      App.prefs().setLastInfo(free, d.getTime(), line);
      MainWidgetProvider.updateAll();
    } catch (IOException e) {
    } catch (JSONException e) {
    } catch (ParseException e) {
    } finally {
      sIsUpdating = false;

      if (wl.isHeld())
        wl.release();
    }
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    updateData();
  }

  public static void start() {
    if (!sIsUpdating)
      App.app().startService(new Intent(App.app(), UpdateService.class));
  }
}
