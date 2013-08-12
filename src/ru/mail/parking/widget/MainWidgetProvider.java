package ru.mail.parking.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import static ru.mail.parking.widget.App.app;
import static ru.mail.parking.widget.Preferences.TimeFormat;

public class MainWidgetProvider extends AppWidgetProvider {
  @Override
  public void onEnabled(Context context) {
    app().watchNetwork(true);
    SmartUpdate.force();
  }

  @Override
  public void onDisabled(Context context) {
    app().watchNetwork(false);
    SmartUpdate.abort();
  }

  @Override
  public void onUpdate(Context context, AppWidgetManager wmgr, int[] appWidgetIds) {
    update(context, wmgr, appWidgetIds);
  }

  private static void update(Context context, AppWidgetManager wmgr, int[] appWidgetIds) {
    int free = App.prefs().getLastPlaces();
    String freeText = (free == -1 ? "???" : String.valueOf(free));

    TimeFormat tf = App.prefs().getTimeFormat();
    boolean showTime = (tf != TimeFormat.none);

    String when = (showTime ? App.prefs().getLastRefresh() : "");

    for (int id: appWidgetIds) {
      RemoteViews frame = new RemoteViews(context.getPackageName(), showTime ? R.layout.widget
                                                                             : R.layout.widget_counter_only);
      frame.setTextViewText(R.id.count, freeText);

      if (showTime)
        frame.setTextViewText(R.id.updated, when);

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

    return mgr.getAppWidgetIds(new ComponentName(app().getPackageName(),
                                                 app().getPackageName() + "." +
                                                 MainWidgetProvider.class.getSimpleName()));
  }

  public static void updateAll() {
    AppWidgetManager mgr = AppWidgetManager.getInstance(app());
    if (mgr != null)
      update(app(), mgr, getWidgetIds());
  }
}