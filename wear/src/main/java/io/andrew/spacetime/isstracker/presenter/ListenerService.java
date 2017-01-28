package io.andrew.spacetime.isstracker.presenter;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.provider.SyncStateContract;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import io.andrew.spacetime.isstracker.R;
import io.andrew.spacetime.isstracker.view.MainActivity;

/**
 * Created by Andrew on 1/18/2017.
 */

public class ListenerService extends WearableListenerService {
  private int notificationId = 001;
  @Override
  public void onMessageReceived(MessageEvent messageEvent) {
    //Log.e("onMessageRecieved", "message recieved");

    //Need to broadcast now instead of using listeners
    Intent messageIntent = new Intent();
    messageIntent.setAction(Intent.ACTION_SEND);
    //String payload = "ISS current latitude: " + lat + " and longitude: " + lon;
    String[] payloadParsed = messageEvent.getPath().split("\\s+");
    //Log.e("messageReceived", ""+payloadParsed.length);
    if(payloadParsed.length==7) {
      //Log.e("messageReceived", "lat " + payloadParsed[3]);
      //Log.e("messageReceived", "lat " + payloadParsed[6]);
      messageIntent.putExtra("lat", payloadParsed[3]);
      messageIntent.putExtra("lng", payloadParsed[6]);
    }

    if(messageEvent.getPath().equals("ISSFLYBYNOTIFY")){
      sendNotification("Iss Flyby!", "ISS will flyby soon! Open to see current ISS location.");
    }
    LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
  }

  private void sendNotification(String title, String content) {

    // this intent will open the activity when the user taps the "open" action on the notification
    Intent viewIntent = new Intent(this, MainActivity.class);
    PendingIntent pendingViewIntent = PendingIntent.getActivity(this, 0, viewIntent, 0);


    NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.ic_action_name)
        .setContentTitle(title)
        .setContentText(content)
        .setContentIntent(pendingViewIntent).setVibrate(new long[] {1000, 1000}).extend(new NotificationCompat.WearableExtender()
            .setContentIcon(R.drawable.ic_action_name));

    Notification notification = builder.build();

    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
    notificationManagerCompat.notify(notificationId++, notification);
  }

}
