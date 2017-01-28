package io.andrew.spacetime.isstracker.model;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import io.andrew.spacetime.isstracker.presenter.listeners.DataContainer;
import io.andrew.spacetime.isstracker.presenter.servicerunner.UpdaterService;
import io.andrew.spacetime.isstracker.view.MainActivity;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Andrew on 1/25/2017.
 */


public class GetNextPass{

  public TestAsyncTask testAsyncTask;
  public String allData = "";
  public DataContainer dataContainer;

  //Async task for MainActivity
  public GetNextPass(MainActivity ma, DataContainer m, double lat, double lng){
    String fullURL = "http://api.open-notify.org/iss-pass.json?lat=" + lat + "&lon=" + lng;
    testAsyncTask = new TestAsyncTask(ma, fullURL);
    dataContainer = m;
    //Log.e("Async", "Started");
  }

  //Async task for UpdaterService
  public GetNextPass(UpdaterService ma, DataContainer m, double lat, double lng){
    String fullURL = "http://api.open-notify.org/iss-pass.json?lat=" + lat + "&lon=" + lng;
    testAsyncTask = new TestAsyncTask(ma, fullURL);
    dataContainer = m;
    //Log.e("Async", "Started");
  }



  public void startAsync(){
    testAsyncTask.execute();
  }

  public class TestAsyncTask extends AsyncTask<Void, Void, String> {
    private Context mContext;
    private String mUrl;

    public TestAsyncTask(Context context, String url) {
      mContext = context;
      mUrl = url;
    }

    @Override protected void onPreExecute() {
      super.onPreExecute();
    }

    @Override protected String doInBackground(Void... params) {
      String resultString = null;
      resultString = getJSON(mUrl);
      allData = resultString;
      return resultString;
    }

    @Override protected void onPostExecute(String strings) {
      super.onPostExecute(strings);
      //Log.e("GetNextPassData", "Data retrieved: " + allData);
      dataContainer.setNextPassData(allData);
    }

    private String getJSON(String iUrl) {
      URL url;
      HttpURLConnection urlConnection = null;
      String output = "";
      //Log.e("Async", "Attempting connection with: " + iUrl);
      try {
        url = new URL(iUrl);

        urlConnection = (HttpURLConnection) url.openConnection();

        InputStream in = urlConnection.getInputStream();

        InputStreamReader isw = new InputStreamReader(in);

        int data = isw.read();
        //Log.e("Website data", "Retrieving data");
        while (data != -1) {
          char current = (char) data;
          data = isw.read();
          output = output + current;
        }
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        if (urlConnection != null) {
          urlConnection.disconnect();
        }
      }
      return output;
    }
  }
}
