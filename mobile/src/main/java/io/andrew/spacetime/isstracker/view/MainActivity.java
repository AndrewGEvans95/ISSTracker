package io.andrew.spacetime.isstracker.view;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import com.google.android.gms.common.api.GoogleApiClient;
import io.andrew.spacetime.isstracker.presenter.listeners.MiscData;
import io.andrew.spacetime.isstracker.model.GetURLData;
import io.andrew.spacetime.isstracker.model.GoogleClientHandler;
import io.andrew.spacetime.isstracker.R;
import io.andrew.spacetime.isstracker.presenter.listeners.NodeObserver;
import io.andrew.spacetime.isstracker.presenter.listeners.Nodes;

public class MainActivity extends AppCompatActivity {



  //Pass this subject to the GoogleClientHandler
  Nodes nodes = new Nodes();
  MiscData miscData = new MiscData();
  NodeObserver n;
  public TextView tView;

    private GoogleApiClient client;
    private GoogleClientHandler clientHandler;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

      tView = (TextView) findViewById(R.id.issData);
      clientHandler = new GoogleClientHandler(nodes);
        Log.e("onCreate", "Executing googleClientApi");

      n = new NodeObserver(nodes, miscData, clientHandler);
      nodes.setState(clientHandler.nodeId);
      Log.e("onCreate", "Observe state set");
        initApi();
        Log.e("onCreate", "googleClientApi was initialized");

      miscData.setState("");
      miscData.setPayload("");

        final GetURLData getData = new GetURLData(MainActivity.this, miscData);
        getData.startAsync();


      final Handler handler = new Handler();
      handler.postDelayed(new Runnable() {
        public void run() {
          //miscData.setState("");
          final GetURLData getData = new GetURLData(MainActivity.this, miscData);
          getData.startAsync();

          //MainActivity.this.n.forceUpdate();
          tView.setText(MainActivity.this.miscData.getPayload());
          //MainActivity.this.miscData.getState();
          handler.postDelayed(this, 500);
        }
      }, 500);
    }


    /**
     * Initializes the GoogleApiClient and gets the Node ID of the connected device.
     */
    private void initApi() {
        client = clientHandler.getGoogleApiClient(this);
        clientHandler.updateClient(client);
        clientHandler.retrieveDeviceNode();
    }


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
