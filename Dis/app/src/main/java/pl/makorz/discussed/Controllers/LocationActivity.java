package pl.makorz.discussed.Controllers;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.List;
import java.util.Locale;
import pl.makorz.discussed.R;

public class LocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    public SupportMapFragment mapFragment;
    private String placeName, countryName, countryCode;
    private Double latitude, longitude;
    private AlertDialog dialog;
    public boolean mapClicked = false;
    private Button acceptButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        acceptButton = findViewById(R.id.button_accept_localisation);
        loadingAlertDialog();

        //Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("placeName", placeName);
                returnIntent.putExtra("countryName", countryName);
                returnIntent.putExtra("countryCode", countryCode);
                returnIntent.putExtra("latitude", latitude);
                returnIntent.putExtra("longitude", longitude);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
    }

    // Save what was allowed by user
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == 6) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
                    }
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setBuildingsEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(@NonNull Location location) {

                LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                dialog.dismiss();
                findAddressInfo(current);
                
                mMap.clear();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 12));
                mMap.addMarker(new MarkerOptions().position(current).title(getString(R.string.map_icon_text_position_location_activity)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {

            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
              locationNotTurnedOnDialog();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},6);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {

                    mMap.clear();
                    mapClicked = true;
                    findAddressInfo(latLng);
                    mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(getString(R.string.map_icon_text_position_location_activity))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));


                }
            });
        }

    @Override
    public void onBackPressed() {
        finish();
    }

    // This function shows AlertDialog
    public void loadingAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogAlertView = inflater.inflate(R.layout.progress_bar, null);
        TextView messageAlertView = dialogAlertView.findViewById(R.id.loading_msg);
        builder.setView(dialogAlertView);
        messageAlertView.setText(R.string.loading_your_localisation_info_dialog_box);
        dialog = builder.create();
        dialog.show();
    }

    public void locationNotTurnedOnDialog() {

        LayoutInflater inflaterDialog = LayoutInflater.from(this);
        View topicsView = inflaterDialog.inflate(R.layout.dialog_localisation_not_turned_on, null);
        AlertDialog localisationNotTurnedOn = new AlertDialog.Builder(this)
                .setView(topicsView)  // What to use in dialog box
                .setNegativeButton(R.string.no_text_dialog_boxes, null)
                .setPositiveButton("OK, take me to settings!", null)
                .show();

        localisationNotTurnedOn.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, 1);
                localisationNotTurnedOn.dismiss();
            }
        });

        localisationNotTurnedOn.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                localisationNotTurnedOn.dismiss();

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},6);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,  1000, 0, locationListener);
            }
        }
    }

    public void findAddressInfo(LatLng latLng) {

        latitude = latLng.latitude;
        longitude = latLng.longitude;
        Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());

        try {
            List<Address> listAddresses = geocoder.getFromLocation(latitude,longitude,2);
            if (listAddresses.size() > 0) {
                if (listAddresses.get(0).getCountryCode() != null && listAddresses.get(0).getLocality() != null && listAddresses.get(0).getCountryName() != null) {
                    countryCode = listAddresses.get(0).getCountryCode();
                    countryName = listAddresses.get(0).getCountryName();
                    placeName = listAddresses.get(0).getLocality();
                    Toast.makeText(LocationActivity.this, getString(R.string.location_accept_location_activity_toast), Toast.LENGTH_SHORT).show();
                    acceptButton.setEnabled(true);
                    locationManager.removeUpdates(locationListener);
                    locationManager = null;
                } else {
                    Toast.makeText(LocationActivity.this, getString(R.string.location_not_accept_location_activity_toast), Toast.LENGTH_SHORT).show();
                    acceptButton.setEnabled(false);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}