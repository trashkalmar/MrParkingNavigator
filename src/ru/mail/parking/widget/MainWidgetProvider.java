package ru.mail.parking.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.RemoteViews;

import ru.mail.parking.R;
import ru.mail.parking.floors.Place;

import static ru.mail.parking.App.app;
import static ru.mail.parking.App.prefs;
import static ru.mail.parking.Preferences.TimeFormat;

public class MainWidgetProvider extends AppWidgetProvider {
  @Override
  public void onEnabled(Context context) {
    app().setHasWidgets(true);
  }

  @Override
  public void onDisabled(Context context) {
    app().setHasWidgets(false);
  }

  @Override
  public void onUpdate(Context context, AppWidgetManager wmgr, int[] appWidgetIds) {
    update(context, wmgr, appWidgetIds);
  }

  private static void update(Context context, AppWidgetManager wmgr, int[] appWidgetIds) {
    Place place = prefs().getStoredPlace();

    int resId;
    String count;
    String info = "";

    if (place == null) {
      int free = prefs().getLastPlaces();
      count = (free == Place.INVALID ? "???" : String.valueOf(free));

      TimeFormat tf = prefs().getTimeFormat();
      boolean showTime = (tf != TimeFormat.none);

      resId = showTime ? R.layout.widget
                       : R.layout.widget_counter_only;
      if (showTime)
        info = prefs().getLastRefresh();
    } else {
      resId = R.layout.widget_place;
      count = String.valueOf(place.getNumber());
      info = app().getString(R.string.floor_format, place.getFloor(), place.getSide().name());
    }


    for (int id: appWidgetIds) {
      RemoteViews frame = new RemoteViews(context.getPackageName(), resId);
      frame.setTextViewText(R.id.count, count);
      if (!TextUtils.isEmpty(info))
        frame.setTextViewText(R.id.info, info);

      Intent it = new Intent(context, MainReceiver.class);
      it.setAction(MainReceiver.ACTION_TAP);

      PendingIntent pi = PendingIntent.getBroadcast(context, 0, it, PendingIntent.FLAG_CANCEL_CURRENT);
      frame.setOnClickPendingIntent(R.id.frame, pi);
      wmgr.updateAppWidget(id, frame);
    }
  }

  public static int[] getWidgetIds() {
    AppWidgetManager mgr = AppWidgetManager.getInstance(app());
    if (mgr == null)
      return new int[0];

    return mgr.getAppWidgetIds(new ComponentName(app().getPackageName(), MainWidgetProvider.class.getName()));
  }

  public static void updateAll() {
    AppWidgetManager mgr = AppWidgetManager.getInstance(app());
    if (mgr != null)
      update(app(), mgr, getWidgetIds());
  }
}