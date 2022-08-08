package com.app.backgroundlocationpractice;

import android.Manifest;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;

public class MainActivity extends AppCompatActivity {
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    LocationCallback callback;
    LocationRequest request;
    private FusedLocationProviderClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        request = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(15000)
                .setFastestInterval(5000);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityResultLauncher<String[]> permissionRequest = this.registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                        Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                        Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                        if (fineLocationGranted != null && fineLocationGranted) {
                            Log.d(this.getLocalClassName(), "Fine location permission granted");
                        }
                        if (coarseLocationGranted != null && coarseLocationGranted) {
                            Log.d(this.getLocalClassName(), "Coarse location permission granted");
                        }
                        if((fineLocationGranted != null && fineLocationGranted) &&
                                (coarseLocationGranted != null && coarseLocationGranted)) {
                            client = LocationServices.getFusedLocationProviderClient(MainActivity.this);
                            callback = new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);
                                    Location location = locationResult.getLastLocation();
                                    ((TextView) findViewById(R.id.tv_coordinates)).setText(location.getLatitude() + "," + location.getLongitude());
                                    Log.d("LOCATION_UPDATES", location.getLatitude() + "," + location.getLongitude());
                                }
                            };
                            enableLocationSettings(MainActivity.this);
                        }
                    });
            permissionRequest.launch(PERMISSIONS);
        } else {
            client = LocationServices.getFusedLocationProviderClient(MainActivity.this);
            callback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    Location location = locationResult.getLastLocation();
                    ((TextView) findViewById(R.id.tv_coordinates)).setText(location.getLatitude() + "," + location.getLongitude());
                    Log.d("LOCATION_UPDATES", location.getLatitude() + "," + location.getLongitude());
                }
            };
            enableLocationSettings(MainActivity.this);

        }
    }

    public void enableLocationSettings(AppCompatActivity activity) {
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(request);
            LocationServices.getSettingsClient(activity).checkLocationSettings(builder.build())
                    .addOnSuccessListener(activity, (LocationSettingsResponse response) -> {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        client.requestLocationUpdates(request, callback, Looper.getMainLooper());
                    }).addOnFailureListener(activity, ex -> {
                        if (ex instanceof ResolvableApiException) {
                            // Location settings are NOT satisfied,  but this can be fixed  by showing the user a dialog.
                            try {
                                ResolvableApiException resolvable = (ResolvableApiException) ex;
                                resolvable.startResolutionForResult(activity, 1);
                            } catch (IntentSender.SendIntentException sendEx) {
                                // Ignore the error.
                            }
                        }
                    });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (client != null) {
            client.removeLocationUpdates(callback);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(client!=null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            client.requestLocationUpdates(request, callback, Looper.getMainLooper());
        }
    }
}