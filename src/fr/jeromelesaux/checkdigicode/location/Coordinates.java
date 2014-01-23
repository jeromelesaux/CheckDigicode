package fr.jeromelesaux.checkdigicode.location;

import java.text.DecimalFormat;

/**
 * Created with IntelliJ IDEA.
 * User: jlesaux
 * Date: 26/04/13
 * Time: 10:52
 * To change this template use File | Settings | File Templates.
 */
public class Coordinates {
    private double longitude;
    private double latitude;

    public Coordinates(double longitude,double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString(){
        DecimalFormat format = new DecimalFormat();
        format.setMaximumFractionDigits(4);
        format.setMinimumFractionDigits(4);
        format.setDecimalSeparatorAlwaysShown(true);
        String latitudeString =  format.format(latitude);
        String longitudeString = format.format(longitude);
        return "Latitude : " + latitudeString + " Longitude : " +longitudeString;
    }
}
