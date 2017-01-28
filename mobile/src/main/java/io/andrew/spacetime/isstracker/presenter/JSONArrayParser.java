package io.andrew.spacetime.isstracker.presenter;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Andrew on 1/26/2017.
 */

public class JSONArrayParser {

  private JSONObject wholeJSON;

  public JSONArrayParser(String arrayResponse){
    try {
      wholeJSON = new JSONObject(arrayResponse);
    }
    catch (JSONException e){
      //Log.d("JSONArrayParser", e.toString());
    }
  }

  public String[] getRiseTimes(){
    JSONArray locArr = new JSONArray();
    String[] riseTimes = new String[5];
    try {
      locArr = wholeJSON.getJSONArray("response");
    }
    catch (JSONException e){
      //Log.d("JSONArrayParser", e.toString());
    }
      for (int i = locArr.length(); i >= 0; i--) {
        try {
          JSONObject childJSONObject = locArr.getJSONObject(i);
          if(childJSONObject.getString("risetime")!=null&&!childJSONObject.getString("risetime").equals("")) {
            riseTimes[i] = childJSONObject.getString("risetime");
          }
        }
        catch (JSONException e){
          //Log.d("JSONArrayParser", e.toString());
        }
    }
    return riseTimes;
  }

}
