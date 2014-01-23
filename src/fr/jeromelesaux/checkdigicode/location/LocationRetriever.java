package fr.jeromelesaux.checkdigicode.location;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: jlesaux
 * Date: 26/04/13
 * Time: 10:51
 * To change this template use File | Settings | File Templates.
 */
public class LocationRetriever {
    private Coordinates coordinates;
    private Context context;

    public LocationRetriever(Context context) {
        this.context = context;
    }

    public Coordinates retreiveCoordinates(String locationName)
        throws IOException {

        String locationNameWithCountry;
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        locationNameWithCountry = locationName + " " + Locale.getDefault().getDisplayCountry();
        List<Address> addressList = null;

        if ( Geocoder.isPresent()) {
            try {
                addressList = geocoder.getFromLocationName(locationNameWithCountry,1);
            } catch (IllegalArgumentException e) {
                Log.w(LocationRetriever.class.getName()," Error in Geocoder reverse geocoding : " + e.getMessage());
            }
        }
        if ( addressList != null &&  addressList.size() != 0 ) {
            coordinates = new Coordinates(addressList.get(0).getLongitude(), addressList.get(0).getLatitude());
        }
        else {
            coordinates = new Coordinates(Constants.MinDouble, Constants.MinDouble);
        }

        Log.w(LocationRetriever.class.getName(),"address " + locationNameWithCountry + " returns Longitude " + coordinates.getLongitude() + " and latitude " + coordinates.getLatitude());
        return coordinates;
    }
}
