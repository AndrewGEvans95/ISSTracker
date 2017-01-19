package io.andrew.spacetime.isstracker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.common.api.GoogleApiClient;

public class MainActivity extends AppCompatActivity {


    private GoogleApiClient client;
    private GoogleClientHandler clientHandler = new GoogleClientHandler();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e("onCreate", "Executing googleClientApi");
        initApi();
        Log.e("onCreate", "googleClientApi was initialized");

        GetURLData getData = new GetURLData(MainActivity.this);
        getData.startAsync();
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
