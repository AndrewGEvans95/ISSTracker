package io.andrew.spacetime.isstracker.view;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import com.google.android.gms.common.api.GoogleApiClient;
import io.andrew.spacetime.isstracker.presenter.listeners.DataContainer;
import io.andrew.spacetime.isstracker.model.GetURLData;
import io.andrew.spacetime.isstracker.model.GoogleClientHandler;
import io.andrew.spacetime.isstracker.R;
import io.andrew.spacetime.isstracker.presenter.listeners.DataObserver;

public class MainActivity extends AppCompatActivity {

  //Initialize and set the DataContainer class
  private DataContainer dataContainer = new DataContainer();

  //Initialize the DataObserver class to watch for changes in DataContainer
  private DataObserver n;

  //Initialize GoogleApiClient to for MainActivity
  private GoogleApiClient client;

  //Initialize GoogleClientHandler for global control of GoogleApiClient outside of MainActivity
  private GoogleClientHandler clientHandler;

  //Initialize any view objects
  public TextView tView;


  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    //Find text view that corresponds with issData and set tView
    tView = (TextView) findViewById(R.id.issData);

    //Set clientHandler to GoogleClientHandler with access to dataContainer object
    clientHandler = new GoogleClientHandler(dataContainer);

    //Set DataObserver Object n with access to dataContainer and clientHandler objects
    n = new DataObserver(dataContainer, clientHandler);

    //Set the current Node Id to the nodeId found by clientHandler
    dataContainer.setCurrentNodeId(clientHandler.nodeId);

    //Initialize GoogleClientApi and pass to GoogleClientHandler for global access to GoogleClientApi
    initApi();

    //Update rawData and payload Strings in dataContainer
    dataContainer.setRawData("");
    dataContainer.setPayloadSilent("");

    //Handler to continuously receive data from webpage
    final Handler handler = new Handler();
    handler.postDelayed(new Runnable() {
      public void run() {
        final GetURLData getData = new GetURLData(MainActivity.this, dataContainer);
        getData.startAsync();
        tView.setText(MainActivity.this.dataContainer.getPayload());
        handler.postDelayed(this, 500);
      }
    }, 500);
  }


  /**
   ** Initializes the GoogleApiClient and gets the Node ID of the connected device.
   **/
  private void initApi() {
    client = clientHandler.getGoogleApiClient(this);
    clientHandler.updateClient(client);
    clientHandler.retrieveDeviceNode();
  }

  /**
   * Cleanup on exit
   * Shutdown all instances of GoogleClientApi
   */
  @Override
  protected void onStop() {
    super.onStop();
    Log.e("onStop", "disconnecting googleClientApi");

    if (client != null)
      if (client.isConnected()) client.disconnect();

    if (clientHandler.client != null){
      if(clientHandler.client.isConnected()) clientHandler.client.disconnect();
    }
  }

}