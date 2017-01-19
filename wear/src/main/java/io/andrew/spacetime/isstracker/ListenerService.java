package io.andrew.spacetime.isstracker;

import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by Andrew on 1/18/2017.
 */

public class ListenerService extends WearableListenerService {

  @Override
  public void onMessageReceived(MessageEvent messageEvent) {
    Log.e("onMessageRecieved", "message recieved");
    //showToast(messageEvent.getPath());
  }

  private void showToast(String message) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
  }
}
