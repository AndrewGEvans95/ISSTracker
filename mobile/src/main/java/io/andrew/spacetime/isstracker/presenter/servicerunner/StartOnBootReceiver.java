package io.andrew.spacetime.isstracker.presenter.servicerunner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Andrew on 1/22/2017.
 */

public class StartOnBootReceiver extends BroadcastReceiver{
  @Override
  public void onReceive(Context context, Intent intent) {

    Intent myIntent = new Intent(context, UpdaterService.class);
    context.startService(myIntent);

  }
}
