package io.andrew.spacetime.isstracker.view;

import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import io.andrew.spacetime.isstracker.model.GetNextPass;
import io.andrew.spacetime.isstracker.presenter.listeners.DataContainer;
import io.andrew.spacetime.isstracker.model.GetURLData;
import io.andrew.spacetime.isstracker.model.GoogleClientHandler;
import io.andrew.spacetime.isstracker.R;
import io.andrew.spacetime.isstracker.presenter.listeners.DataObserver;
import io.andrew.spacetime.isstracker.presenter.servicerunner.UpdaterService;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
    LocationListener {

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
  public TextView connectionStatus;
  public ListView nextPassList;
  ArrayAdapter adapter;

  private GoogleMap mMap;
  private MapFragment mMapFragment;

  public double gpsLat = 0;
  public double gpsLng = 0;
  public int notified = 0;

  private boolean mapIsReady = false;

  Marker issMarker;

  LocationManager mLocationManager;


  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED) {
      mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
      try {
          mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
      }
      catch (SecurityException e){
        tView.setText("Please enable GPS");
        //Log.d("SECURITY CATCH", e.toString());
      }

    } else {
      ActivityCompat.requestPermissions(this, new String[] {
              Manifest.permission.ACCESS_FINE_LOCATION,
              Manifest.permission.ACCESS_COARSE_LOCATION }, 0);
      mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
      try {
        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
          mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
      }
      catch (SecurityException e){
        tView.setText("Please enable GPS");
        //Log.d("SECURITY CATCH", e.toString());
      }
    }

    //Find text view that corresponds with issData and set tView
    tView = (TextView) findViewById(R.id.issData);
    connectionStatus = (TextView) findViewById(R.id.connectionState);
    nextPassList = (ListView) findViewById(R.id.nextPassList);
    tView.setText("Loading data...");
    String[] emptyString = new String[1];
    emptyString[0] = "Acquiring current location...";
    adapter = new ArrayAdapter<String>(this, R.layout.activity_listview, emptyString);
    nextPassList.setAdapter(adapter);
    mMapFragment = (MapFragment) getFragmentManager()
        .findFragmentById(R.id.map);
    mMapFragment.getMapAsync(this);

    //Stop background service from sending data to watch while UI is sending data to watch
    stopService(new Intent(this, UpdaterService.class));


    //Set clientHandler to GoogleClientHandler with access to dataContainer object
    clientHandler = new GoogleClientHandler(dataContainer);

    //Set DataObserver Object n with access to dataContainer and clientHandler objects
    n = new DataObserver(dataContainer, clientHandler);

    //Set the current Node Id to the nodeId found by clientHandler
    dataContainer.setCurrentNodeId(clientHandler.nodeId);

    //Initialize GoogleClientApi and pass to GoogleClientHandler for global access to GoogleClientApi
    initApi();

    //Update rawData, payload, and nextPassData Strings in dataContainer
    dataContainer.setRawData("");
    dataContainer.setPayloadSilent("");
    dataContainer.setNextPassData("");


    //Handler to send data to phone and update phone app
    final Handler handler = new Handler();
    handler.postDelayed(new Runnable() {
      public void run() {
        final GetURLData getData = new GetURLData(MainActivity.this, dataContainer);
        getData.startAsync();
        if(MainActivity.this.dataContainer.getCurrentNodeId()==null){
          connectionStatus.setText("Error: no wear device detected");
        }
        else{
          connectionStatus.setText("Connected to: " + MainActivity.this.dataContainer.getCurrentNodeId());
        }
        String[] payloadParsed = MainActivity.this.dataContainer.getPayload().split("\\s+");
        //Log.e("messageReceived", ""+payloadParsed.length);
        if(payloadParsed.length==7) {
          //Log.e("messageReceived", "lat " + payloadParsed[3]);
          //Log.e("messageReceived", "lat " + payloadParsed[6]);
          double lat = Double.parseDouble(payloadParsed[3]);
          double lng = Double.parseDouble(payloadParsed[6]);

          tView.setText(lat + ", " + lng);

          if (mapIsReady) {
            LatLng issLocation = new LatLng(lat, lng);
            if(issMarker!=null) {
              //issMarker.setPosition(issLocation);
              animateMarker(issMarker, issLocation, false);
            }
            if(issMarker==null) {
              issMarker = mMap.addMarker(new MarkerOptions().position(issLocation)
                  .title("ISS")
                  .snippet("Population: 6")
                  .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_name)));
            }

            // Move the camera to show the marker.
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(issLocation, mMap.getCameraPosition().zoom));
          }
        }

        handler.postDelayed(this, 1000);
      }
    }, 1000);

    //Handler to check next pass data
    //Handler to send data to phone and update phone app
    final Handler nextPassHandler = new Handler();
    nextPassHandler.postDelayed(new Runnable() {
      public void run() {
        final GetNextPass getNP = new GetNextPass(MainActivity.this, dataContainer, gpsLat, gpsLng);
        getNP.startAsync();
        if(MainActivity.this.dataContainer.getNextPassDataPayload()!=null&&!MainActivity.this.dataContainer.getNextPassDataPayload().equals(""))
        {
          //nextPassText.setText(MainActivity.this.dataContainer.getNextPassDataPayload());
          String[] newPassArray = MainActivity.this.dataContainer.getNextPassArray();
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
              formattedArray[i] = sdf.format(date);
            }
          }
          if(formattedArray!=null&&formattedArray.length>0) {
            adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.activity_listview,
                formattedArray);
            nextPassList.setAdapter(adapter);
          }
        }

        nextPassHandler.postDelayed(this, 1000);
      }
    }, 1000);
  }


  public void animateMarker(final Marker marker, final LatLng toPosition,
      final boolean hideMarker) {
    final Handler handler = new Handler();
    final long start = SystemClock.uptimeMillis();
    Projection proj = mMap.getProjection();
    Point startPoint = proj.toScreenLocation(marker.getPosition());
    final LatLng startLatLng = proj.fromScreenLocation(startPoint);
    final long duration = 500;
    final Interpolator interpolator = new LinearInterpolator();
    handler.post(new Runnable() {
      @Override
      public void run() {
        long elapsed = SystemClock.uptimeMillis() - start;
        float t = interpolator.getInterpolation((float) elapsed
            / duration);
        double lng = t * toPosition.longitude + (1 - t)
            * startLatLng.longitude;
        double lat = t * toPosition.latitude + (1 - t)
            * startLatLng.latitude;
        marker.setPosition(new LatLng(lat, lng));
        if (t < 1.0) {
          // Post again 16ms later.
          handler.postDelayed(this, 16);
        } else {
          if (hideMarker) {
            marker.setVisible(false);
          } else {
            marker.setVisible(true);
          }
        }
      }
    });
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
  public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap;
    LatLng issPoint = new LatLng(-33.85704, 151.21522);
    mMap.moveCamera(CameraUpdateFactory.newLatLng(issPoint));
    mapIsReady = true;
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
    //Start background service to continue sending data to watch without UI activity
    startService(new Intent(this, UpdaterService.class));
    super.onStop();
    //clientHandler.sendData("ISSFLYBYNOTIFY");
    //Log.e("onStop", "disconnecting googleClientApi");

    if (client != null)
      if (client.isConnected()) client.disconnect();

    if (clientHandler.client != null){
      if(clientHandler.client.isConnected()) clientHandler.client.disconnect();
    }
  }

}