package io.andrew.spacetime.isstracker.presenter.listeners;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import io.andrew.spacetime.isstracker.model.GoogleClientHandler;
import io.andrew.spacetime.isstracker.presenter.PayloadBuilder;
import io.andrew.spacetime.isstracker.view.MainActivity;

/**
 * Created by Andrew on 1/19/2017.
 */

public class NodeObserver extends Observer{
  GoogleClientHandler gClient;
  public NodeObserver(Nodes nodes, MiscData miscData, GoogleClientHandler g){
    this.nodes = nodes;
    this.nodes.attach(this);

    this.miscData = miscData;
    this.miscData.attach(this);

    gClient = g;
  }

  @Override
  public void update() {
    if(nodes.getState() != null && nodes.getState()!="") {
      Log.e("NodeObserver", "Node was updated: " + nodes.getState());
      //Execute sendData now
    }
    if(miscData.getState()!=null && miscData.getState()!=""){
      Log.e("NodeObserver", "MiscData was updated: " + miscData.getState());
      PayloadBuilder pB = new PayloadBuilder(miscData.getState());
      String payload = pB.craftLatAndLong();
      Log.e("NodeObserver", payload);
    }

    if(nodes.getState()!=null&&nodes.getState()!=""  && miscData.getState()!=null&&miscData.getState()!=""){
      Log.e("NodeObserver", "Both Node and MiscData have been set, sending data to Node");
      PayloadBuilder pB = new PayloadBuilder(miscData.getState());
      String payload = pB.craftLatAndLong();
      miscData.setPayload(payload);
      gClient.sendData(payload);
    }
  }

  @Override
  public void forceUpdate(){
    Log.e("NodeObserver", "forcing update");
    if(nodes.getState()!=null&&nodes.getState()!=""  && miscData.getState()!=null&&miscData.getState()!="") {
      PayloadBuilder pB = new PayloadBuilder(miscData.getState());
      String payload = pB.craftLatAndLong();
      gClient.sendData(payload);
      miscData.setPayload(payload);
    }

  }

}