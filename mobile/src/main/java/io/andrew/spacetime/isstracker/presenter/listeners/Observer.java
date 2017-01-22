package io.andrew.spacetime.isstracker.presenter.listeners;

/**
 * Created by Andrew on 1/19/2017.
 */

public abstract class Observer{

  protected DataContainer dataContainer;

  public abstract void update();

}