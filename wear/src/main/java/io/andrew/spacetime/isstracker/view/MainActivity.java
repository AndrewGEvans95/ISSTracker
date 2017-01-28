package io.andrew.spacetime.isstracker.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.DismissOverlayView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import io.andrew.spacetime.isstracker.R;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends WearableActivity implements OnMapReadyCallback,
    GoogleMap.OnMapLongClickListener{

  private static final SimpleDateFormat AMBIENT_DATE_FORMAT = new SimpleDateFormat("HH:mm", Locale.US);

  private static final LatLng SYDNEY = new LatLng(-33.85704, 151.21522);
  Marker issMarker;
  private DismissOverlayView mDismissOverlay;

  private boolean mapIsReady = false;

  private GoogleMap mMap;
  private MapFragment mMapFragment;

  private TextView currentTime;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    setAmbientEnabled();



    // Retrieve the containers for the root of the layout and the map. Margins will need to be
    // set on them to account for the system window insets.
    final FrameLayout topFrameLayout = (FrameLayout) findViewById(R.id.container);
    final FrameLayout mapFrameLayout = (FrameLayout) findViewById(R.id.map_container);
    currentTime = (TextView) findViewById(R.id.currentTime );
    currentTime.setGravity(Gravity.CENTER | Gravity.BOTTOM);

    // Set the system view insets on the containers when they become available.
    topFrameLayout.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
      @Override
      public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
        // Call through to super implementation and apply insets
        insets = topFrameLayout.onApplyWindowInsets(insets);

        FrameLayout.LayoutParams params =
            (FrameLayout.LayoutParams) mapFrameLayout.getLayoutParams();

        // Add Wearable insets to FrameLayout container holding map as margins
        params.setMargins(
            insets.getSystemWindowInsetLeft(),
            insets.getSystemWindowInsetTop(),
            insets.getSystemWindowInsetRight(),
            insets.getSystemWindowInsetBottom());
        mapFrameLayout.setLayoutParams(params);

        currentTime.setLayoutParams(params);
        return insets;
      }
    });

    // Obtain the DismissOverlayView and display the intro help text.
    mDismissOverlay = (DismissOverlayView) findViewById(R.id.dismiss_overlay);
    mDismissOverlay.setIntroText(R.string.intro_text);
    mDismissOverlay.showIntroIfNecessary();

    // Obtain the MapFragment and set the async listener to be notified when the map is ready.
    mMapFragment = (MapFragment) getFragmentManager()
        .findFragmentById(R.id.map);
    mMapFragment.getMapAsync(this);

    currentTime.setText("Loading...");

    //mContainerView = (BoxInsetLayout) findViewById(R.id.container);
    //mTextView = (TextView) findViewById(R.id.text);
    final Handler handler = new Handler();
    handler.postDelayed(new Runnable() {
      public void run() {
        DateFormat df = new SimpleDateFormat("h:mm a");
        String date = df.format(Calendar.getInstance().getTime());
        currentTime.setText(date);
        handler.postDelayed(this, 1000);
      }
    }, 1000);

    //This will receive the bundled message from the ListenerService class
    IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
    MessageReceiver messageReceiver = new MessageReceiver();
    LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);

  }

  //Receives bundled message from BroadcastReceiver
  public class MessageReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent.getStringExtra("lat") != null) {
        String sLat = intent.getStringExtra("lat");
        String sLng = intent.getStringExtra("lng");
        double lat = Double.parseDouble(sLat);
        double lng = Double.parseDouble(sLng);
        // Display message in UI
        //mTextView.setText(message);
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
    }
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

  @Override public void onEnterAmbient(Bundle ambientDetails) {
    super.onEnterAmbient(ambientDetails);
    mMapFragment.onEnterAmbient(ambientDetails);
    //updateDisplay();
  }

  @Override public void onUpdateAmbient() {
    super.onUpdateAmbient();
    //updateDisplay();
  }

  @Override public void onExitAmbient() {
    mMapFragment.onExitAmbient();
    //updateDisplay();
    super.onExitAmbient();
  }


  @Override
  public void onMapReady(GoogleMap googleMap) {
    // Map is ready to be used.
    mMap = googleMap;

    // Set the long click listener as a way to exit the map.
    mMap.setOnMapLongClickListener(this);

    // Move the camera to show the marker.
    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SYDNEY, 1));
    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
    //mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));
    mapIsReady=true;
  }

  @Override
  public void onMapLongClick(LatLng latLng) {
    // Display the dismiss overlay with a button to exit this activity.
    /*
    if(mMap.getMapType()==GoogleMap.MAP_TYPE_HYBRID) {
      mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
    }
    else{
      mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }
    */
    mDismissOverlay.show();
  }
}
