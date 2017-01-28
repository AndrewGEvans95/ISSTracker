package io.andrew.spacetime.isstracker.presenter.servicerunner;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import com.google.android.gms.common.api.GoogleApiClient;
import io.andrew.spacetime.isstracker.R;
import io.andrew.spacetime.isstracker.model.GetNextPass;
import io.andrew.spacetime.isstracker.model.GetURLData;
import io.andrew.spacetime.isstracker.model.GoogleClientHandler;
import io.andrew.spacetime.isstracker.presenter.listeners.DataContainer;
import io.andrew.spacetime.isstracker.presenter.listeners.DataObserver;
import io.andrew.spacetime.isstracker.view.MainActivity;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Andrew on 1/22/2017.
 */



public class UpdaterService extends Service implements LocationListener {


  //Initialize and set the DataContainer class
  private DataContainer dataContainer = new DataContainer();

  //Initialize the DataObserver class to watch for changes in DataContainer
  private DataObserver n;

  //Initialize GoogleApiClient to for MainActivity
  private GoogleApiClient client;

  //Initialize GoogleClientHandler for global control of GoogleApiClient outside of MainActivity
  private GoogleClientHandler clientHandler;

  public double gpsLat = 0;
  public double gpsLng = 0;

  public int notified = 0;

  LocationManager mLocationManager;


  @Override
  public IBinder onBind(Intent arg0) {
    return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    //Handle commands here
    //Log.e("UpdateService", "Starting service");
    runClient();
    //Log.e("UpdateService", "running client from service");
    // We want this service to continue running until it is explicitly
    // stopped, so return sticky.
    return START_STICKY;
  }

  public void runClient(){

    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED) {
      mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
      try {
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
      }
      catch (SecurityException e){
        //Log.d("SECURITY CATCH", e.toString());
      }

    } else {
      try {
        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
      } catch (SecurityException e) {
        //Log.d("SECURITY CATCH", e.toString());
      }
    }


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

    final Handler handler = new Handler();
    handler.postDelayed(new Runnable() {
      public void run() {
    final GetURLData getData = new GetURLData(UpdaterService.this, dataContainer);
    getData.startAsync();
    dataContainer.getPayload();
        handler.postDelayed(this, 5000);
      }
    }, 5000);


    //Handler to check next pass data
    //Handler to send data to phone and update phone app
    final Handler nextPassHandler = new Handler();
    nextPassHandler.postDelayed(new Runnable() {
      public void run() {
        final GetNextPass getNP = new GetNextPass(UpdaterService.this, dataContainer, gpsLat, gpsLng);
        getNP.startAsync();
        if(UpdaterService.this.dataContainer.getNextPassDataPayload()!=null&&!UpdaterService.this.dataContainer.getNextPassDataPayload().equals(""))
        {
          //nextPassText.setText(MainActivity.this.dataContainer.getNextPassDataPayload());
          String[] newPassArray = UpdaterService.this.dataContainer.getNextPassArray();
          String[] formattedArray = new String[newPassArray.length];

          for(int i=0; i<formattedArray.length; i++){
            if(newPassArray[i]!=null&&!newPassArray[i].equals("")) {
              long timestamp = Long.parseLong(newPassArray[i]);
              long currentTime = System.currentTimeMillis();
              long remainingTime = timestamp-currentTime;
              Date date = new Date(timestamp * 1000L); // *1000 is to convert seconds to milliseconds
              Date remainingDate = new Date(remainingTime * 1000L);


              long delta = ((timestamp*1000L) - currentTime) / 1000L;
              // calculate (and subtract) whole days
              long days = delta / 86400L;
              delta -= days * 86400L;

              // calculate (and subtract) whole hours
              long hours = (delta / 3600L) % 24;
              delta -= hours * 3600;

              // calculate (and subtract) whole minutes
              long minutes = (delta / 60L) % 60;
              delta -= minutes * 60;

              // what's left is seconds
              long seconds = delta % 60;  // in theory the modulus is not required


              SimpleDateFormat sdf = new SimpleDateFormat("MM/dd @ hh:mm a z");
              SimpleDateFormat hoursRemaining = new SimpleDateFormat("hh a");

              //Check every minute and if missed 30 minute mark then try again at 29
              if(((hours*60)+ minutes) == 30 && notified==0){
                //Log.d("UpdaterService", "SENDING NOTIFCATION TO WEAR");
                clientHandler.sendData("ISSFLYBYNOTIFY");
                notified=1;
              }
              if(((hours*60)+ minutes) == 29 && notified==0){
                clientHandler.sendData("ISSFLYBYNOTIFY");
                notified=1;
              }
              if(((hours*60)+ minutes)!=29 || ((hours*60)+ minutes)!=30 && notified==1){
                notified=0;
              }
            }
          }
        }

        nextPassHandler.postDelayed(this, 60000);
      }
    }, 60000);
  }

  /**
   ** Initializes the GoogleApiClient and gets the Node ID of the connected device.
   **/
  private void initApi() {
    client = clientHandler.getGoogleApiClient(this);
    clientHandler.updateClient(client);
    clientHandler.retrieveDeviceNode();
  }


  public void onLocationChanged(Location location) {
    if (location != null) {
      //Log.d("Location Changed", location.getLatitude() + " and " + location.getLongitude());
      gpsLat=location.getLatitude();
      gpsLng=location.getLatitude();
      try {
        mLocationManager.removeUpdates(this);
      }
      catch (SecurityException e){
        //Log.d("SECURITY CATCH", e.toString());
      }
    }
  }

  // Required functions
  public void onProviderDisabled(String arg0) {}
  public void onProviderEnabled(String arg0) {}
  public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}

  @Override
  public void onDestroy() {

    //Cleanup before exiting
    client.disconnect();
    clientHandler.client.disconnect();
    super.onDestroy();
  }
}
