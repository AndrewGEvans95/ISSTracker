package io.andrew.spacetime.isstracker.presenter;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Andrew on 1/20/2017.
 */

public class JSONParser {
  private String JSONData;
  private JSONObject data;
  private JSONObject rec;

  public JSONParser(String j){
    JSONData = j;
    try {
      data = new JSONObject(j);
    }
    catch (JSONException e){
      //Log.e("JSONParser", e.toString());
    }
  }

  public String getLat(){
    String lat = "";
    rec = new JSONObject();
    try {
      rec = data.getJSONObject("iss_position");
    }
    catch (JSONException e){
      //Log.e("JSONParser", e.toString());
    }
    try {
      lat = rec.getString("latitude");
    }
    catch (JSONException e){
      //Log.e("JSONParser", e.toString());
    }
    return lat;
  }

  public String getLong() {
    String lon = "";
    rec = new JSONObject();
    try {
      rec = data.getJSONObject("iss_position");
    } catch (JSONException e) {
      //Log.e("JSONParser", e.toString());
    }
    try {
      lon = rec.getString("longitude");
    } catch (JSONException e) {
      //Log.e("JSONParser", e.toString());
    }
    return lon;
  }
}
