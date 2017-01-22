package io.andrew.spacetime.isstracker.presenter.listeners;

import android.content.Context;
import android.os.Handler;
import android.widget.TextView;
import io.andrew.spacetime.isstracker.view.MainActivity;

/**
 * Created by Andrew on 1/19/2017.
 */

public abstract class Observer{
  protected Nodes nodes;
  protected MiscData miscData;
  public abstract void forceUpdate();
  public abstract void update();
}