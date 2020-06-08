package com.example.mapapplication;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{
//Other Implementations LocationListener,GoogleApiClient.ConnectionCallbacks,
//        GoogleApiClient.OnConnectionFailedListener

    private GoogleMap mMap;
    private boolean mLocationPermissionGranted;
    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    Marker mCurrLocationMarker;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private EditText searchText ;
    private Button searchButton ;
    private FirebaseAuth mAuth ;
    private List<Works> listData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        searchButton=(Button) findViewById(R.id.search_button);
        searchText = (EditText) findViewById(R.id.search_text);

        mAuth = FirebaseAuth.getInstance();

        getDeviceLocation();

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchString = searchText.getText().toString();
                Geocoder geocoder = new Geocoder(MapsActivity.this);

                List<Address> list = new ArrayList<>();
                try {
                    list = geocoder.getFromLocationName(searchString,1);
                }
                catch (IOException e)
                {
                    Toast.makeText(MapsActivity.this, "Search Failed:"+e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                }

                if(list.size()>0)
                {
                    Address address = list.get(0);
                    markAddress(address);
                }
            }
        });




    }

    private void markAddress(Address address) {

        Toast.makeText(this, "Mark Address", Toast.LENGTH_SHORT).show();
        double latitude = address.getLatitude();
        double longitude = address.getLongitude();
        LatLng myLocation = new LatLng(latitude, longitude);

        MarkerOptions markerOptions = new MarkerOptions();
        mCurrLocationMarker = mMap.addMarker(new MarkerOptions().position(myLocation).title("Searched Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.moveCamera( CameraUpdateFactory.newLatLng( myLocation ) );

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        getLocationPermission();

        if (mLocationPermissionGranted) {
            getDeviceLocation();
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setAllGesturesEnabled(true);

        }

        DatabaseReference Rootref = FirebaseDatabase.getInstance().getReference();

        DatabaseReference workRef = Rootref.child("Works").child(mAuth.getCurrentUser().getUid());

        listData = new ArrayList<>();


        workRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        Works l = dataSnapshot1.getValue(Works.class);
                        listData.add(l);
                    }

                    markLocations(listData);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void markLocations(List<Works> listData) {
        Toast.makeText(this, "Mark Address", Toast.LENGTH_SHORT).show();

        for ( Works var : listData){
            String[] address =var.getLocation().split(",");
            double latitude = Double.valueOf(address[0]);
            double longitude = Double.valueOf(address[1]);
            LatLng myLocation = new LatLng(latitude, longitude);

            MarkerOptions markerOptions = new MarkerOptions();
            mCurrLocationMarker = mMap.addMarker(new MarkerOptions().position(myLocation).title(var.getTitle()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));

        }

    }


    private void getLocationPermission() {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }


    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }

    }



    public void getDeviceLocation()
    {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            Task location = mFusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful())
                    {
                        Location mCurrLocation = (Location) task.getResult();
                        if(mCurrLocation!=null)
                            updateLocation(mCurrLocation);
                    }
                    else
                    {
                        Toast.makeText(MapsActivity.this, "Unable to get Location", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
        catch (SecurityException e)
        {
            Toast.makeText(this, "Security Exception:"+e.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }

    }

    private void updateLocation(Location mCurrLocation) {

        double latitude = mCurrLocation.getLatitude();
        double longitude = mCurrLocation.getLongitude();
        LatLng myLocation = new LatLng(latitude, longitude);
        mCurrLocationMarker = mMap.addMarker(new MarkerOptions().position(myLocation).title("My Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
    }


}
