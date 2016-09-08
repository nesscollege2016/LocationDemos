package tomerbu.edu.locationdemos;

import android.app.ProgressDialog;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


//https://developer.android.com/training/location/change-location-settings.html#get-settings
public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener {

    private static final int REQUEST_CODE_LOCATION = 10;
    private ProgressDialog dialog;
    private GoogleMap map;
    private GoogleApiClient mApiClient;
    private LocationRequest mLocationRequest;
    private EditText etCity;
    private EditText etCountry;
    private EditText etStreet;
    private int REQUEST_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        etCity = (EditText) findViewById(R.id.etCity);
        etCountry = (EditText) findViewById(R.id.etCountry);
        etStreet = (EditText) findViewById(R.id.etStreet);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = new SupportMapFragment();

        getSupportFragmentManager().beginTransaction().
                replace(R.id.mapContainer, mapFragment).commit();

        mapFragment.getMapAsync(this);

        showDialog();

        initRadioGroup();

    }

    @Override
    protected void onStart() {
        super.onStart();
        initApiClient();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocationServices.FusedLocationApi.removeLocationUpdates(mApiClient, this);
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


    //Init the Google api client, specifying LocationServices as the api
    private void initApiClient() {
        mApiClient = new GoogleApiClient.
                Builder(this).
                addConnectionCallbacks(this).
                addApi(LocationServices.API).build();
        //Async:
        mApiClient.connect();
        //when connected?: onConnected
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //If we got the permission:
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                && requestCode == REQUEST_CODE_LOCATION) {
            requestLocation();
        }
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

        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(1000);

        testLocation(request);

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        requestLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double longitude = location.getLongitude();

        LatLng loc = new LatLng(lat, longitude);

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 17));

        getAddress(location);
    }

    //Reverse GeoCoding
    private void getAddress(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            Address address = addresses.get(0);

            StringBuilder builder = new StringBuilder();

            int max = address.getMaxAddressLineIndex();
            for (int i = 0; i <= max; i++) {
                builder.append(address.getAddressLine(i)).append("\n");
            }

            Toast.makeText(MapsActivity.this, builder.toString(), Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void testLocation(final LocationRequest locationRequest) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        LocationSettingsRequest settingsRequest = builder.build();

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mApiClient, settingsRequest);

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult res) {
                Status status = res.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(MapsActivity.this, REQUEST_LOCATION);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        break;
                    case LocationSettingsStatusCodes.SUCCESS:
                        Toast.makeText(MapsActivity.this, "Location Available", Toast.LENGTH_SHORT).show();

                        startLocationUpdates(locationRequest);
                        break;
                    default:
                        Toast.makeText(MapsActivity.this, "No Location In Device", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void startLocationUpdates(LocationRequest request) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient,
                request,
                this);
    }

    public void searchAddress(View view) {
        String city = etCity.getText().toString();
        String country = etCountry.getText().toString();
        String address =etStreet.getText().toString();

        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addressList =
                    geocoder.getFromLocationName(address + ", " + city + ", " + country, 1);

            Address a = addressList.get(0);
            LatLng location = new LatLng(a.getLatitude(), a.getLongitude());
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 17));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
