package hu.kite.szamtech.proba.photopos1;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * Created by Joco on 2014.11.02..
 */
public class MyLocationProvider implements LocationListener {

    // A konstruktor elindítja a GPS alapú hely lekérést
    // TODO le kall állítani valamikor ! A Stop metódust mindenképpen meg kell hívni.

    LocationManager manager;
    boolean gpsenabled = false;

    public MyLocationProvider(LocationManager manager) {
        this.manager = manager;
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
        gpsenabled = true;
    }

    @Override
    public void onProviderDisabled(String s) {
        gpsenabled = false;
    }

    public void stopgps() {
        manager.removeUpdates(this);

    }

    public boolean isGpsEnabled(){
        return false;
    }

}
