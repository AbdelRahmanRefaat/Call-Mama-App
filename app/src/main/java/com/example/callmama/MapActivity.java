package com.example.callmama;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback
, GoogleApiClient.OnConnectionFailedListener{


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

        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        initWidgets();
      //  AutoCompleteSearch();


    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    // constants
    private static final String TAG = "MapActivity";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 555;
    private static final int AUTOCOMPLETION_REQUEST_CODE = 666;

    private static final float DEFAULT_ZOOM = 15f; // Zoom Takse Values from 0  to 21

    // widgets
    private AutoCompleteTextView inputSearchText;
    private Button goSearch;

    // vars
    private GoogleMap mMap;
    private boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mCurrentLocation;
    private GoogleApiClient mGoogleApiClient;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private PolylineOptions routeTrackerOptions;
    private Polyline routeTracker;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;


    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40,-168) , new LatLng(71,136));


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        getDeviceLocationPermission();
        getDeviceLocation();
        startRouteTracking();
        //
        if (!com.google.android.libraries.places.api.Places.isInitialized()) {
            com.google.android.libraries.places.api.Places.initialize(getApplicationContext(), String.valueOf(R.string.google_map_api_key));
        }

    }
    /*
    * -initing the necessary functions for the route tracing
    * -and start route tracking
    * */
    private void startRouteTracking(){

        initRouteTracker(); // inition the routeTrackerOptions & Polyline
        // the properties of the call back e.g The interval on which we will periodicly request the updates of the location
        initLocationRequestProperties();
        // start requesting the updates about our current location
        startLocationUpdates();
    }
    /*
    *  inition the routeTrackerOptions & Polyline
    *  also inition LocationCall back & drawing the route so far
    * */
    private void initRouteTracker(){
        Log.d(TAG, "initRouteTracker: initing route tracker ..");
        routeTrackerOptions = new PolylineOptions();
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult){
                        if(locationResult == null)
                                return;
                Log.d(TAG, "onLocationResult: locatiosn found are " + locationResult.getLocations().size());
                        for(Location location : locationResult.getLocations()){
                                moveCamera(new LatLng(location.getLatitude()
                                ,location.getLongitude()),mMap.getCameraPosition().zoom);
                                routeTrackerOptions.add(new LatLng(location.getLatitude(),
                                        location.getLongitude()));
                        }
                        routeTrackerOptions.color(Color.CYAN);
                        routeTracker =mMap.addPolyline(routeTrackerOptions);
            }
        };

    }
    private void initLocationRequestProperties(){
                locationRequest = locationRequest.create();
                locationRequest.setInterval(3000);
                locationRequest.setFastestInterval(3000);
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    private  void startLocationUpdates(){
            mCurrentLocation.requestLocationUpdates(locationRequest
            ,locationCallback, Looper.getMainLooper());
    }

    void initWidgets(){
        Log.d(TAG, "initWidgets: initing widgets");
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient,
                LAT_LNG_BOUNDS, null);

        inputSearchText = (AutoCompleteTextView) findViewById(R.id.textSearchET);
        //inputSearchText.setAdapter(mPlaceAutocompleteAdapter);
        inputSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView textView, int action, KeyEvent keyEvent) {
                Log.d(TAG, "onEditorAction: we Entered the Editor Action");
                GeoLocate();
                return false;
            }
        });
    }

//    private void AutoCompleteSearch(){
//        // Set the fields to specify which types of place data to
//        // return after the user has made a selection.
//        List<Place.Field> fields = Arrays.asList(Place.Field.ID,Place.Field.NAME);
//        // Start the autocomplete intent.
//        Intent intent = new Autocomplete.IntentBuilder(
//                AutocompleteActivityMode.FULLSCREEN, fields)
//                .build(this);
//        startActivityForResult(intent, AUTOCOMPLETION_REQUEST_CODE);
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                 Place place = Autocomplete.getPlaceFromIntent(data);
                   Log.d(TAG, "onActivityResult: Place" + place.getName() + place.getAddress());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
            }
        }
    }

    private void GeoLocate() {
        String searchInput = inputSearchText.getText().toString();
        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> ADSlist =  new ArrayList<>();
        try {
            ADSlist = geocoder.getFromLocationName(searchInput, 1);
        }catch (IOException e){
            Log.d(TAG, "GeoLocate: " + e.getMessage());
        }
        if(ADSlist.size() > 0){
            Address address = ADSlist.get(0);
            Log.d(TAG, "GeoLocate: Location is = " + address);
            moveCamera(new LatLng(address.getLatitude(),address.getLongitude()),DEFAULT_ZOOM);
            MarkerOptions mk = new MarkerOptions().position(new LatLng(address.getLatitude(),address.getLongitude()))
                    .title("Location Bla Bla");
            mMap.addMarker(mk);
        }else{
            Log.d(TAG, "GeoLocate: There is no Location found...");
        }

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
