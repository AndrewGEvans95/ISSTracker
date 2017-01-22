package io.andrew.spacetime.isstracker.presenter.listeners;

/**
 * Created by Andrew on 1/20/2017.
 */

import java.util.ArrayList;
import java.util.List;

public class MiscData {

  private List<Observer> observers = new ArrayList<Observer>();
  private String state;
  private String payload;

  public String getState() {
    return state;
  }
  public String getPayload() {return payload;}

  public void setState(String state) {
    this.state = state;
    notifyAllObservers();
  }

  public void setPayload(String payload){
    this.payload = payload;
    //notifyAllObservers();
  }

  public void attach(Observer observer){
    observers.add(observer);
  }

  public void notifyAllObservers(){
    for (Observer observer : observers) {
      observer.update();
    }
  }
}