package tomerbu.edu.locationdemos;

import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.RadioGroup;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks {

    private static final int REQUEST_CODE_LOCATION = 10;
    private ProgressDialog dialog;
    private GoogleMap map;
    private GoogleApiClient mApiClient;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = new SupportMapFragment();

        getSupportFragmentManager().beginTransaction().
                replace(R.id.mapContainer, mapFragment).commit();

        mapFragment.getMapAsync(this);

        showDialog();

        initRadioGroup();

        initApiClient();
    }

    private void initApiClient() {
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(this);

        builder.addApi(LocationServices.API).
                addConnectionCallbacks(this);

        mApiClient = builder.build();
        mApiClient.connect();
    }

    private void initRadioGroup() {
        RadioGroup rgMapType = (RadioGroup) findViewById(R.id.rgMapType);
        rgMapType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                switch (id) {
                    case R.id.radioHybrid:
                        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        break;
                    case R.id.radioNormal:
                        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        break;
                    case R.id.radioSatellite:
                        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        break;
                }
            }
        });
    }

    private void showDialog() {
        dialog = new ProgressDialog(this);
        dialog.setTitle("Loading your map");
        dialog.setMessage("Please wait");
        dialog.show();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        dialog.dismiss();
        this.map = googleMap;


        //College position / Coordinates
        LatLng nessCollege = new LatLng(31.2634545, 34.8094539);

        //Add a marker
        googleMap.addMarker(new MarkerOptions().position(nessCollege));

        //Move the camera (With Animation) To college coordinates, Zoom 17
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(nessCollege, 17));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("Ness", "Connected");
        requestLocation();
    }

    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            //Request the Location Permission
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    REQUEST_CODE_LOCATION);
            //Get out of this method, Since we don't have the permission yet.
            return;
        }
        //If we don't have permission, we don't get here

        //Get the last known location. May be null.!!!
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mApiClient);

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(1000 * 10).
                setFastestInterval(1000).
                setMaxWaitTime(1000).
                setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mApiClient, mLocationRequest, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                         LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                         map.addMarker(new MarkerOptions().position(loc));
                         map.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16));
                    }
                }
        );


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //If we got the permission:
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                && requestCode==REQUEST_CODE_LOCATION){
            requestLocation();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
