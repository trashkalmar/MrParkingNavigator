package ru.mail.parking.sw2.system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SwReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    context.startService(intent.setClass(context, SwService.class));
  }
}
