package io.andrew.spacetime.isstracker.presenter.listeners;

/**
 * Created by Andrew on 1/22/2017.
 */

import java.util.ArrayList;
import java.util.List;

public class DataContainer {

  private List<Observer> observers = new ArrayList<Observer>();

  //Data that has been retrieved from GetURLData
  private String rawData;

  //Data that has been retrieved from GetNextPAss
  private String nextPassData;

  private String nextPassDataPayload;

  private String[] nextPassArray;

  //String will be set by update method in NodeObserver class
  /**
   * miscData.setPayload(payload);
   */
  private String payload;

  //String that will be set to the nodeID of the bluetooth device
  private String currentNodeId;

  public String getCurrentNodeId(){ return currentNodeId; }
  public String getRawData() {
    return rawData;
  }
  public String getNextPassData(){return nextPassData;}
  public String getPayload() {return payload;}
  public String getNextPassDataPayload() {return nextPassDataPayload;}
  public String[] getNextPassArray(){return nextPassArray;}

  //Update the value of setNodeId
  public void setCurrentNodeId(String currentNodeId) {
    this.currentNodeId = currentNodeId;
    notifyAllObservers();
  }

  //Update the value of rawData and notify all Observers
  public void setRawData(String rawData) {
    this.rawData = rawData;
    notifyAllObservers();
  }

  //Update the value of nextPassData and notify all Observers
  public void setNextPassData(String nextPassData){
    this.nextPassData = nextPassData;
    notifyAllObservers();
  }

  //Update the value of payload and notify all Observers
  public void setPayload(String payload){
    this.payload = payload;
    notifyAllObservers();
  }

  //Update the value of payload without notifying Observers
  public void setPayloadSilent(String payload){
    this.payload = payload;
  }

  //Update the value of nextPassPayload without notifying Observers
  public void setNextPassDataPayloadSilent(String nextPassDataPayload) { this.nextPassDataPayload=nextPassDataPayload; }

  //Updated the value of nextPassArray without notifying Observers
  public void setNextPassArraySilent(String[] nextPassArray){ this.nextPassArray=nextPassArray; }

  public void attach(Observer observer){
    observers.add(observer);
  }

  public void notifyAllObservers(){
    for (Observer observer : observers) {
      observer.update();
    }

  }

}