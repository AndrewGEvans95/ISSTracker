package io.andrew.spacetime.isstracker.presenter.listeners;

import android.util.Log;
import io.andrew.spacetime.isstracker.model.GoogleClientHandler;
import io.andrew.spacetime.isstracker.presenter.PayloadBuilder;

/**
 * Created by Andrew on 1/19/2017.
 */

public class DataObserver extends Observer{
  GoogleClientHandler gClient;
  public DataObserver(DataContainer dataContainer, GoogleClientHandler g){

    this.dataContainer = dataContainer;
    this.dataContainer.attach(this);

    gClient = g;
  }

  @Override
  public void update() {

    //Only send to gClient if wear device is connected
    if(dataContainer.getCurrentNodeId()!=null && !dataContainer.getCurrentNodeId().equals("") && dataContainer.getRawData()!=null && !dataContainer.getRawData().equals("")){
      //Log.e("NodeObserver", "Both Node and MiscData have been set, sending data to Node");
      PayloadBuilder pB = new PayloadBuilder(dataContainer.getRawData());
      String payload = pB.craftLatAndLong();
      dataContainer.setPayloadSilent(payload);
      gClient.sendData(payload);
    }


    //Otherwise it is okay to send data to phone app instead of both phone and wear
    if(dataContainer.getRawData()!=null && !dataContainer.getRawData().equals("")){
      //Log.e("NodeObserver", "Both Node and MiscData have been set, sending data to Node");
      PayloadBuilder pB = new PayloadBuilder(dataContainer.getRawData());
      String payload = pB.craftLatAndLong();
      dataContainer.setPayloadSilent(payload);
    }

    //Create nextPassData payload
    if(dataContainer.getNextPassData()!=null && !dataContainer.getNextPassData().equals("")){
      //PayloadBuilder pB = new PayloadBuilder(dataContainer.getNextPassData());
      //String payload = pB.craftNextPass();
      //Not parsed yet, just want to test data retrieval
      PayloadBuilder pB = new PayloadBuilder(dataContainer.getNextPassData());
      String payload = pB.craftNextPass();
      dataContainer.setNextPassDataPayloadSilent(payload);
      String[] nextPassArray = pB.getTimestampArray();
      dataContainer.setNextPassArraySilent(nextPassArray);
    }

  }

}