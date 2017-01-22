package io.andrew.spacetime.isstracker.presenter.listeners;

/**
 * Created by Andrew on 1/19/2017.
 */

import java.util.ArrayList;
import java.util.List;

public class Nodes {

  private List<Observer> observers = new ArrayList<Observer>();
  private String state;

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
    notifyAllObservers();
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