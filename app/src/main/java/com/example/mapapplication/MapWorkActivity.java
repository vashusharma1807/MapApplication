package com.example.mapapplication;

import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapWorkActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText searchText;
    private Button searchButton , addWork;
    private LatLng currLocation = null ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_work);

        searchButton =(Button) findViewById(R.id.search_button_work);
        searchText = (EditText) findViewById(R.id.search_text_work);

        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchString = searchText.getText().toString();
                Geocoder geocoder = new Geocoder(MapWorkActivity.this);

                List<Address> list = new ArrayList<>();
                try {
                    list = geocoder.getFromLocationName(searchString,1);
                }
                catch (IOException e)
                {
                    Toast.makeText(MapWorkActivity.this, "Search Failed:"+e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                }

                if(list.size()>0)
                {
                    Address address = list.get(0);
                    moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), 15f,
                            address.getAddressLine(0));


                    markAddress(address);
                }
            }
        });

        addWork = (Button) findViewById(R.id.add_work_button);

        addWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currLocation!=null) {
                    String loca = String.valueOf(currLocation.latitude)+","+String.valueOf(currLocation.longitude);

                    Intent intent = new Intent(MapWorkActivity.this, AddWorkActivity.class);
                    intent.putExtra("location", loca);
                    startActivity(intent);

                }
            }
        });

    }

    private void moveCamera(LatLng latLng, float zoom, String title){

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if(!title.equals("My Location")){
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(options);
        }

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void markAddress(Address address) {
        Toast.makeText(this, "Mark Address", Toast.LENGTH_SHORT).show();
        double latitude = address.getLatitude();
        double longitude = address.getLongitude();
        LatLng myLocation = new LatLng(latitude, longitude);
        mMap.clear();
        MarkerOptions markerOptions = new MarkerOptions();
        mMap.addMarker(new MarkerOptions().position(myLocation).title("Searched Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        mMap.moveCamera( CameraUpdateFactory.newLatLng( myLocation ) );
        currLocation = myLocation ;

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                MarkerOptions markerOptions = new MarkerOptions();

                markerOptions.position(latLng);

                markerOptions.title(latLng.latitude + " : " + latLng.longitude);

                mMap.clear();

                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                mMap.addMarker(markerOptions);

                currLocation = latLng ;
            }
        });


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.setDraggable(true);
                Toast.makeText(MapWorkActivity.this, "Marker Info"+marker.getPosition(), Toast.LENGTH_SHORT).show();
                moveCamera(marker.getPosition(),15f,marker.getTitle());
                return false;
            }
        });

    }


}
