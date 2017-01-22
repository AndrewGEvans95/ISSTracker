package io.andrew.spacetime.isstracker.presenter;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by Andrew on 1/18/2017.
 */

public class ListenerService extends WearableListenerService {

  @Override
  public void onMessageReceived(MessageEvent messageEvent) {
    Log.e("onMessageRecieved", "message recieved");

    //Need to broadcast now instead of using listeners
    Intent messageIntent = new Intent();
    messageIntent.setAction(Intent.ACTION_SEND);
    messageIntent.putExtra("message", messageEvent.getPath());
    LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
  }

}
