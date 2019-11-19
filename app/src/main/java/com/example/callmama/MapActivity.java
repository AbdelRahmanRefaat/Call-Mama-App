package com.example.callmama;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {


    /*
     *
     *   in this method you can do whatever you want with your map as it's ready else you'll get RTE
     * */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this,"Map Is Ready",Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: Map is Ready...");
        // setting the map
        mMap = googleMap;
        // map type
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        // enable my location to appear in the map
        mMap.setMyLocationEnabled(true);


    }
    // constants
    private static final String TAG = "MapActivity";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 555;
    private static final float DEFAULT_ZOOM = 15f; // Zoom Takse Values from 0  to 21

    // vars
    private GoogleMap mMap;
    private boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mCurrentLocation;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        getDeviceLocationPermission();
        getDeviceLocation();

    }

    private void initMap(){

        Log.d(TAG, "initMap: Initializing the map...");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.Map);
        mapFragment.getMapAsync(MapActivity.this);
    }
    private void getDeviceLocation(){

            if(mLocationPermissionGranted == false) // the user didn't approve opening GPS
                    return ;

            mCurrentLocation = LocationServices.getFusedLocationProviderClient(MapActivity.this);
            try{
                final Task location = mCurrentLocation.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()){

                                Toast.makeText(MapActivity.this,"Found Your Location",Toast.LENGTH_SHORT).show();
                                Location nowLocation = new Location((Location)task.getResult());
                                if(nowLocation != null){
                                        Log.d(TAG, "onComplete: Found Your Location " + nowLocation );
                                        mMap.addMarker( new MarkerOptions().position(new LatLng(nowLocation.getLatitude(),nowLocation.getLongitude()))
                                        .title("My Location"));
                                        moveCamera(new LatLng(nowLocation.getLatitude(),nowLocation.getLongitude()),DEFAULT_ZOOM);
                                }else{
                                    Toast.makeText(MapActivity.this,"Location Not Found...",Toast.LENGTH_SHORT).show();
                                }

                            }
                    }
                });
            }catch (SecurityException E){

            }

    }
    private void getDeviceLocationPermission(){
        Log.d(TAG, "getDeviceLocationPermission: getting the device's location permissions");

        // The Permission we asked for is granted so we proceed
        if(ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED){
            // The Permission we asked for is granted so we proceed
          if(ContextCompat.checkSelfPermission(MapActivity.this,Manifest.permission.ACCESS_COARSE_LOCATION)
          == PackageManager.PERMISSION_GRANTED) {
              mLocationPermissionGranted = true;
              initMap();
          }else{
              /*
               *   if  the request was granted it will init the map it self
               *
               * */
              ActivityCompat.requestPermissions(this,
                      new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                      ,LOCATION_PERMISSION_REQUEST_CODE);


          }


        }else{
            /*
            *   if  the request was granted it will init the map it self
            *
            * */
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                    ,LOCATION_PERMISSION_REQUEST_CODE);
        }
    }



    private void moveCamera(LatLng toGoto, float zoom){
        Log.d(TAG, "moveCamera: Moving Camera " + toGoto + " Zoom = " + zoom);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(toGoto
        ,zoom));
    }

    public void setmMap(GoogleMap mMap) {
        this.mMap = mMap;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;

        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; ++i){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                          mLocationPermissionGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission denied");
                          return ;
                        }
                    }
                    mLocationPermissionGranted = true;
                    Log.d(TAG, "onRequestPermissionsResult: permission granted..");
                    initMap();
                } 
            }
        }

    }
}
