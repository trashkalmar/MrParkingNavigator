package ru.mail.parking.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import static ru.mail.parking.widget.Preferences.TimeFormat;

public class MainWidgetProvider extends AppWidgetProvider {
  @Override
  public void onEnabled(Context context) {
    SmartUpdate.force();
  }

  @Override
  public void onDisabled(Context context) {
    SmartUpdate.abort();
  }

  @Override
  public void onUpdate(Context context, AppWidgetManager wmgr, int[] appWidgetIds) {
    update(context, wmgr, appWidgetIds);
  }

  public static void update(Context context, AppWidgetManager wmgr, int[] appWidgetIds) {
    int free = App.prefs().getLastPlaces();
    String freeText = (free == -1 ? "???" : String.valueOf(free));

    TimeFormat tf = App.prefs().getTimeFormat();
    boolean show = (tf != TimeFormat.none);

    String when = (show ? App.prefs().getLastUpdate() : "");

    for (int id: appWidgetIds) {
      RemoteViews frame = new RemoteViews(context.getPackageName(), R.layout.widget);
      frame.setTextViewText(R.id.count, freeText);

      frame.setViewVisibility(R.id.updated, show ? View.VISIBLE : View.GONE);
      if (show)
        frame.setTextViewText(R.id.updated, when);

      Intent it = new Intent(context, MainReceiver.class);
      it.setAction(MainReceiver.ACTION_CLICK);

      PendingIntent pi = PendingIntent.getBroadcast(context, 0, it, PendingIntent.FLAG_CANCEL_CURRENT);
      frame.setOnClickPendingIntent(R.id.frame, pi);
      wmgr.updateAppWidget(id, frame);
    }
  }

  public static void updateAll() {
    AppWidgetManager mgr = AppWidgetManager.getInstance(App.app());
    if (mgr == null) return;

    int[] ids = mgr.getAppWidgetIds(new ComponentName(App.app().getPackageName(),
                                                      "." + MainWidgetProvider.class.getSimpleName()));
    update(App.app(), mgr, ids);
  }
}