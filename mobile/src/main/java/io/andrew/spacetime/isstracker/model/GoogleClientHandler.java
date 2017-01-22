package io.andrew.spacetime.isstracker.model;

import android.content.Context;
import android.util.Log;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import io.andrew.spacetime.isstracker.presenter.listeners.DataContainer;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Andrew on 1/18/2017.
 */

public class GoogleClientHandler extends Thread{
  private static final long CONNECTION_TIME_OUT_MS = 1000;

  public GoogleApiClient client;
  public String nodeId;
  public Thread rThread;
  DataContainer sub;

  public GoogleClientHandler(DataContainer s){
    sub=s;
  }

  /**
   * Returns a GoogleApiClient that can access the Wear API.
   * @param context
   * @return A GoogleApiClient that can make calls to the Wear API
   */
  public GoogleApiClient getGoogleApiClient(Context context) {
    return new GoogleApiClient.Builder(context).addApi(Wearable.API).build();
  }

  /**
   * Connects to the GoogleApiClient and retrieves the connected device's Node ID. If there are
   * multiple connected devices, the first Node ID is returned.
   */
  public void retrieveDeviceNode() {
    rThread = new Thread(new Runnable() {
      @Override
      public void run() {
        Log.e("retrieveDeviceNode", "Looking for device");
        client.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
        NodeApi.GetConnectedNodesResult result = Wearable.NodeApi.getConnectedNodes(client).await();
        List<Node> nodes = result.getNodes();
        if (nodes.size() > 0) {
          Log.e("retrieveDeviceNode", "Device found: " + nodes.get(0).getDisplayName());
          //Get the first node in the list
          nodeId = nodes.get(0).getId();
          //set the current NodeID and trigger update for all observers of currentNodeId in DataContainer
          sub.setCurrentNodeId(nodeId);
        }
        else{
          Log.e("retrieveDeviceNode", "No devices found");
        }
        Log.e("retrieveDeviceNode", "disconnecting from client");
        client.disconnect();
      }
    });
    rThread.start();
  }

  public void sendData(final String payload) {
    //Check if nodeId is set before attempting to send data
    if (nodeId != null) {
      //Send data to device using thread
      new Thread(new Runnable() {
        String p = payload;
        @Override
        public void run() {
          //Connect to device
          client.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
          //Send payload to device
          Wearable.MessageApi.sendMessage(client, nodeId, p, null);
          //Disconnect after data sent
          client.disconnect();
        }
      }).start();
    }
  }
  public void updateClient(GoogleApiClient cT){
    client=cT;
  }
}
