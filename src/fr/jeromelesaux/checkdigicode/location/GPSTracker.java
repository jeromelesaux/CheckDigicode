package fr.jeromelesaux.checkdigicode.location;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: jlesaux
 * Date: 26/04/13
 * Time: 11:13
 * To change this template use File | Settings | File Templates.
 */
public class GPSTracker extends Service implements LocationListener {
    private final Context context;

    private final long timeout = (10 * 1024);

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;

    Location location; // location
    Coordinates coordinates;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;

    public GPSTracker(Context context) {
        this.context = context;
        coordinates = new Coordinates(Constants.MinDouble, Constants.MinDouble);
        getLocation();
    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) context
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    getLocationProvider(LocationManager.NETWORK_PROVIDER);
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        getLocationProvider(LocationManager.GPS_PROVIDER);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    private void getLocationProvider(String provider) {
        if (locationManager != null) {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

            location = locationManager
                    .getLastKnownLocation(provider);
            if (location != null) {
                coordinates.setLongitude(location.getLongitude());
                coordinates.setLatitude(location.getLatitude());
            } else {
                boolean waitingForLocation = true;
                long currentTime;
                long startTrackingTime = System.currentTimeMillis();
                long timeOut = timeout;
                while (waitingForLocation) {

                    location = locationManager
                            .getLastKnownLocation(provider);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Log.w(GPSTracker.class.getName(), "Error while waiting for "+provider+" : " + e.getMessage());
                    }
                    if (location != null && location.getLatitude() != 0.0 && location.getLongitude() != 0.0) {
                        coordinates.setLongitude(location.getLongitude());
                        coordinates.setLatitude(location.getLatitude());
                        waitingForLocation = false;
                    }
                    currentTime = System.currentTimeMillis();
                    if ( (currentTime - startTrackingTime) >= timeOut) {
                        waitingForLocation = false;
                    }

                }

            }
        }
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     */
    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(GPSTracker.this);
        }
    }

    /**
     * Function to get latitude
     */
    public double getLatitude() {
        if (location != null) {
            coordinates.setLatitude(location.getLatitude());
        }

        // return latitude
        return coordinates.getLatitude();
    }

    /**
     * Function to get longitude
     */
    public double getLongitude() {
        if (location != null) {
            coordinates.setLongitude(location.getLongitude());
        }

        // return longitude
        return coordinates.getLongitude();
    }

    /**
     * Function to check GPS/wifi enabled
     *
     * @return boolean
     */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }
     
 /*   */

    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     *//*
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
      
        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");
  
        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
  
        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        });
  
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            }
        });
  
        // Showing Alert Message
        alertDialog.show();
    }
 */
    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {

    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
