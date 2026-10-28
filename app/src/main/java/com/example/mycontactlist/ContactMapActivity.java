
package com.example.mycontactlist;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import android.Manifest;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;



public class ContactMapActivity extends AppCompatActivity
        implements OnMapReadyCallback, SensorEventListener {

    private SensorManager sensorManager;

    private Sensor accelerometer;

    private Sensor magnetometer;

    private TextView textHeading;

    private float[] gravityValues;

    private float[] magneticValues;


    private GoogleMap gMap;
    private FusedLocationProviderClient fusedLocationClient;

    private final ArrayList<LatLng> contactLocations = new ArrayList<>();

    private static final int LOCATION_PERMISSION_REQUEST = 101;

    private int pendingGeocodes = 0;
    private boolean contactsFinishedLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contact_map);

        textHeading = findViewById(R.id.textHeading);

        sensorManager = (SensorManager)
                getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(
                    Sensor.TYPE_ACCELEROMETER
            );

            magnetometer = sensorManager.getDefaultSensor(
                    Sensor.TYPE_MAGNETIC_FIELD
            );
        }

        if (accelerometer == null || magnetometer == null) {
            textHeading.setText("Compass unavailable");
        }


        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }



    @Override
    protected void onResume() {
        super.onResume();

        if (sensorManager != null) {
            if (accelerometer != null) {
                sensorManager.registerListener(
                        this,
                        accelerometer,
                        SensorManager.SENSOR_DELAY_UI
                );
            }

            if (magnetometer != null) {
                sensorManager.registerListener(
                        this,
                        magnetometer,
                        SensorManager.SENSOR_DELAY_UI
                );
            }
        }
    }

    @Override
    protected void onPause(){
        if (sensorManager !=null) {
            sensorManager.unregisterListener(this);
        }

        super.onPause();
    }

    private String getDirection(float degrees) {
        if (degrees >= 337.5 || degrees < 22.5) {
            return "N";
        } else if (degrees < 67.5) {
            return "NE";
        } else if (degrees < 112.5) {
            return "E";
            } else if (degrees < 157.5) {
            return "SE";
        } else if (degrees < 202.5) {
            return "S";
        } else if (degrees < 247.5) {
            return "SW";
        } else if (degrees < 292.5) {
            return "W";
        } else {
            return "NW";
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravityValues = event.values.clone();
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticValues = event.values.clone();
        }
        if (gravityValues == null || magneticValues == null) {
            return;
        }
        float[] rotationMatrix = new float[9];
        float[] inclinationMatrix = new float[9];

        boolean success = SensorManager.getRotationMatrix(
                rotationMatrix,
                inclinationMatrix,
                gravityValues,
                magneticValues
        );

        if (!success) {
            return;
        }
        float[] orientation = new float[3];

        SensorManager.getOrientation(
                rotationMatrix,
                orientation
        );

        float azimuth = (float) Math.toDegrees(orientation[0]);
        azimuth = (azimuth + 360) % 360;

        String direction = getDirection(azimuth);
        textHeading.setText(
                String.format(
                        java.util.Locale.US,
                        "%s %.0f°",
                        direction,
                        azimuth
                )
        );

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {

        gMap = googleMap;
        gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        gMap.setPadding(0, 120, 0, 0);

        if (hasLocationPermission()) {
            enableMyLocation();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST
            );
        }

        loadContacts();
    }

    private boolean hasLocationPermission() {

        boolean fineLocation =
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        boolean coarseLocation =
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        return fineLocation || coarseLocation;
    }

    private void enableMyLocation() {

        if (gMap == null || !hasLocationPermission()) {
            return;
        }

        try {

            gMap.setMyLocationEnabled(true);

            gMap.getUiSettings()
                    .setMyLocationButtonEnabled(true);

            moveToCurrentLocation();

        } catch (SecurityException e) {

            Log.e("CONTACT_MAP", "Location permission error", e);

            Toast.makeText(
                    this,
                    "Location permission error",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void moveToCurrentLocation() {

        if (!hasLocationPermission() || gMap == null) {
            return;
        }

        try {

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {

                        if (location != null && gMap != null) {

                            LatLng currentPosition = new LatLng(
                                    location.getLatitude(),
                                    location.getLongitude()
                            );

                            gMap.addMarker(
                                    new MarkerOptions()
                                            .position(currentPosition)
                                            .title("My Current Location")
                            );

                            Log.d(
                                    "CONTACT_MAP",
                                    "Current location: "
                                            + currentPosition.latitude
                                            + ", "
                                            + currentPosition.longitude
                            );

                            if (contactsFinishedLoading
                                    && contactLocations.isEmpty()) {

                                gMap.animateCamera(
                                        CameraUpdateFactory.newLatLngZoom(
                                                currentPosition,
                                                15f
                                        )
                                );
                            }

                        } else {

                            Log.d(
                                    "CONTACT_MAP",
                                    "Current location unavailable"
                            );
                        }
                    })
                    .addOnFailureListener(this, e -> {

                        Log.e(
                                "CONTACT_MAP",
                                "Unable to retrieve location",
                                e
                        );
                    });

        } catch (SecurityException e) {

            Log.e("CONTACT_MAP", "Location permission error", e);
        }
    }

    private void loadContacts() {

        ContactDataSource dataSource =
                new ContactDataSource(this);

        ArrayList<Contact> contacts;

        try {

            dataSource.open();
            contacts = dataSource.getContacts();

        } catch (Exception e) {

            Log.e("CONTACT_MAP", "Unable to load contacts", e);

            Toast.makeText(
                    this,
                    "Unable to load contacts",
                    Toast.LENGTH_LONG
            ).show();

            return;

        } finally {
            dataSource.close();
        }

        for (Contact contact : contacts) {

            if (contact.getStreetAddress() != null
                    && !contact.getStreetAddress().trim().isEmpty()) {

                pendingGeocodes++;
                addContactMarker(contact);
            }
        }

        Toast.makeText(
                this,
                "Contacts loaded: " + contacts.size(),
                Toast.LENGTH_LONG
        ).show();

        if (pendingGeocodes == 0) {
            contactsFinishedLoading = true;
        }
    }

    private void addContactMarker(Contact contact) {

        String fullAddress =
                contact.getStreetAddress() + ", " +
                        contact.getCity() + ", " +
                        contact.getState() + " " +
                        contact.getZipCode();

        Log.d(
                "CONTACT_MAP",
                "Looking up contact: "
                        + contact.getContactName()
                        + " | Address: "
                        + fullAddress
        );

        if (!Geocoder.isPresent()) {

            Log.e("CONTACT_MAP", "Geocoder unavailable");
            finishGeocode();
            return;
        }

        Geocoder geocoder = new Geocoder(this);

        geocoder.getFromLocationName(
                fullAddress,
                1,
                new Geocoder.GeocodeListener() {

                    @Override
                    public void onGeocode(List<Address> addresses) {

                        runOnUiThread(() -> {

                            if (!addresses.isEmpty()) {

                                Address address = addresses.get(0);

                                LatLng position = new LatLng(
                                        address.getLatitude(),
                                        address.getLongitude()
                                );

                                Log.d(
                                        "CONTACT_MAP",
                                        "Contact: " + contact.getContactName()
                                                + " | Address: " + fullAddress
                                                + " | Latitude: " + position.latitude
                                                + " | Longitude: " + position.longitude
                                );

                                if (gMap != null) {

                                    gMap.addMarker(
                                            new MarkerOptions()
                                                    .position(position)
                                                    .title(contact.getContactName())
                                                    .snippet(fullAddress)
                                    );

                                    contactLocations.add(position);
                                }

                            } else {

                                Log.w(
                                        "CONTACT_MAP",
                                        "No coordinates for: " + fullAddress
                                );
                            }

                            finishGeocode();
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {

                        Log.e(
                                "CONTACT_MAP",
                                "Geocoder error: " + errorMessage
                        );

                        runOnUiThread(() -> finishGeocode());
                    }
                }
        );
    }

    private void finishGeocode() {

        pendingGeocodes--;

        if (pendingGeocodes == 0) {

            contactsFinishedLoading = true;
            showAllContacts();
        }
    }

    private void showAllContacts() {

        if (gMap == null || contactLocations.isEmpty()) {
            return;
        }

        if (contactLocations.size() == 1) {

            gMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                            contactLocations.get(0),
                            15f
                    )
            );

        } else {

            LatLngBounds.Builder builder =
                    new LatLngBounds.Builder();

            for (LatLng position : contactLocations) {
                builder.include(position);
            }

            LatLngBounds bounds = builder.build();

            gMap.setOnMapLoadedCallback(() -> {

                gMap.animateCamera(
                        CameraUpdateFactory.newLatLngBounds(
                                bounds,
                                150
                        )
                );
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == LOCATION_PERMISSION_REQUEST) {

            if (hasLocationPermission()) {

                enableMyLocation();

            } else {

                Toast.makeText(
                        this,
                        "Location permission was denied",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }
}
