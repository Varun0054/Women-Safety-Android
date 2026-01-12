package com.example.womensafe;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class locationn extends AppCompatActivity implements OnMapReadyCallback {

    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 101;
    private GoogleMap mMap;

    private Marker userMarker;
    private Circle accuracyCircle;

    // Map type state
    private int currentMapType = GoogleMap.MAP_TYPE_NORMAL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locationn);

        fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(this);

        Button btnRefresh = findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(v -> fetchLocation());

        Button btnMapType = findViewById(R.id.btnMapType);
        btnMapType.setOnClickListener(v -> switchMapType());

        fetchLocation();
    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE
            );
            return;
        }

        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                if (location != null) {
                    currentLocation = location;

                    String info =
                            "Lat: " + location.getLatitude() +
                                    "\nLng: " + location.getLongitude() +
                                    "\nAccuracy: " + location.getAccuracy() + " m";

                    Toast.makeText(locationn.this, info, Toast.LENGTH_LONG).show();

                    SupportMapFragment mapFragment =
                            (SupportMapFragment) getSupportFragmentManager()
                                    .findFragmentById(R.id.myMap);

                    if (mapFragment != null) {
                        mapFragment.getMapAsync(locationn.this);
                    }

                } else {
                    Toast.makeText(
                            locationn.this,
                            "Unable to fetch location!",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Apply map type
        mMap.setMapType(currentMapType);

        // Enable map UI features
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);

        if (currentLocation != null) {

            LatLng latLng = new LatLng(
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude()
            );

            if (userMarker != null) userMarker.remove();
            if (accuracyCircle != null) accuracyCircle.remove();

            userMarker = mMap.addMarker(
                    new MarkerOptions()
                            .position(latLng)
                            .title("You are here")
            );

            accuracyCircle = mMap.addCircle(
                    new CircleOptions()
                            .center(latLng)
                            .radius(currentLocation.getAccuracy())
                            .strokeWidth(2f)
                            .strokeColor(0x550000FF)
                            .fillColor(0x220000FF)
            );

            mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(latLng, 16)
            );

        } else {
            Toast.makeText(
                    this,
                    "Location not available!",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void switchMapType() {
        if (mMap == null) return;

        if (currentMapType == GoogleMap.MAP_TYPE_NORMAL) {
            currentMapType = GoogleMap.MAP_TYPE_TERRAIN;
            Toast.makeText(this, "Terrain View", Toast.LENGTH_SHORT).show();

        } else if (currentMapType == GoogleMap.MAP_TYPE_TERRAIN) {
            currentMapType = GoogleMap.MAP_TYPE_SATELLITE;
            Toast.makeText(this, "Satellite View", Toast.LENGTH_SHORT).show();

        } else if (currentMapType == GoogleMap.MAP_TYPE_SATELLITE) {
            currentMapType = GoogleMap.MAP_TYPE_HYBRID;
            Toast.makeText(this, "Hybrid View", Toast.LENGTH_SHORT).show();

        } else {
            currentMapType = GoogleMap.MAP_TYPE_NORMAL;
            Toast.makeText(this, "Normal View", Toast.LENGTH_SHORT).show();
        }

        mMap.setMapType(currentMapType);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                fetchLocation();

            } else {
                Toast.makeText(
                        this,
                        "Permission denied!",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }
}
