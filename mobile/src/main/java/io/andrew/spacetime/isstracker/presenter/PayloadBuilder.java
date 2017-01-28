package io.andrew.spacetime.isstracker.presenter;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Andrew on 1/20/2017.
 */

public class PayloadBuilder {

  private JSONParser jP;
  private JSONArrayParser jAP;
  private String toParse;

  public PayloadBuilder(String s){
    toParse=s;
    jP = new JSONParser(toParse);
    jAP = new JSONArrayParser(toParse);
  }

  public String craftNextPass(){
    String[] riseTimes = jAP.getRiseTimes();
    String payload = "";
    for(int i=0; i<riseTimes.length; i++){
      if(riseTimes[i]!=null&&!riseTimes[i].equals("")) {
        long timestamp = Long.parseLong(riseTimes[i]);

        Date date = new Date(timestamp*1000L); // *1000 is to convert seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("dd hh:mm a z");
        String formattedDate = sdf.format(date);
        payload = formattedDate + ", " + payload;
      }
    }
    return payload;
  }

  public String[] getTimestampArray(){
    return jAP.getRiseTimes();
  }

  public String craftLatAndLong(){
    String lat = jP.getLat();
    String lon = jP.getLong();
    String payload = "ISS current latitude: " + lat + " and longitude: " + lon;
    return payload;
  }
}
