package io.andrew.spacetime.isstracker.presenter;

/**
 * Created by Andrew on 1/20/2017.
 */

public class PayloadBuilder {

  private JSONParser jP;
  private String toParse;

  public PayloadBuilder(String s){
    toParse=s;
    jP = new JSONParser(toParse);
  }

  public String craftLatAndLong(){
    String lat = jP.getLat();
    String lon = jP.getLong();
    String payload = "ISS current latitude: " + lat + " and longitude: " + lon;
    return payload;
  }
}
