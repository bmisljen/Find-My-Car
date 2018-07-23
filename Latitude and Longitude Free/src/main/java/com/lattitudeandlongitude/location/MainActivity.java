package com.lattitudeandlongitude.location;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.os.Handler;
import android.os.Looper;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.widget.AdapterView;
import android.widget.EditText;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.text.InputType;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.Serializable;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class MainActivity extends Activity {

    LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationSettingsRequest mLocationSettingsRequest;
    private Location mCurrentLocation;
    private LocationCallback mLocationCallback;
    private ArrayList<Double> arr = new ArrayList<Double>();
    private LinkedHashMap<String, ArrayList<Double>> hashmap = new LinkedHashMap<String, ArrayList<Double>>();
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private String m_Text = "";


    /**
     * Removes location updates from the FusedLocationApi.
     */
    private void stopLocationUpdates() {

        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
    }

    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                    mLocationCallback, Looper.myLooper());
                            //onLocationChanged();

                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }

                        //onLocationChanged();
                    }
                });
    }

    /**
     * Creates a callback for receiving location events.
     */
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                mCurrentLocation = locationResult.getLastLocation();
                onLocationChanged();
            }
        };
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation,
                        R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        File file = new File(getDir("data", MODE_PRIVATE), "hashmap");
        try {
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));
            try {
                hashmap = (LinkedHashMap) inputStream.readObject();
            } catch (ClassNotFoundException e1)
            {
            }
            inputStream.close();
        } catch (IOException e) {
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        // Kick off the process of building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects.
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();

        // start the location updates
        if (checkPermissions()) {
            startLocationUpdates();
        } else {
            requestPermissions();
        }

        Button map = (Button) findViewById(R.id.button2);
        map.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
              //start the map
              Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:"+mCurrentLocation.getLatitude()+","+mCurrentLocation.getLongitude()+"?q="+mCurrentLocation.getLatitude()+","+mCurrentLocation.getLongitude()+"(current Location)"));
              startActivity(intent);
            }
        });

        Button saveLocation = (Button) findViewById(R.id.button3);
        saveLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // add the location to the arraylist
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Save Location");

                // Set up the input
                final EditText input = new EditText(MainActivity.this);
                input.setHint("Type a location name");
                 // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        m_Text = input.getText().toString();
                        // add the new coordinate to the hashmap
                        arr.clear();
                        arr.add(mCurrentLocation.getLatitude());
                        arr.add(mCurrentLocation.getLongitude());
                        hashmap.put(m_Text, arr);
                        String txt = "Location " + m_Text + " added to Saved List";
                        Toast toast = Toast.makeText(MainActivity.this, txt, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
                builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });

        Button restoreLocation = (Button) findViewById(R.id.button4);
        restoreLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // add the locations to a char array
                CharSequence[] savedLocations = new CharSequence[hashmap.size()];
                int arrIndex = 0;
                for ( String key : hashmap.keySet() ) {
                    savedLocations[arrIndex++] = key;
                }

                // create an AlrtDialog to display this to the user
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Select Location (Long Click to Delete)");
                builder.setItems(savedLocations, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        // start the map to navigate to the location
                        arr = (new ArrayList<ArrayList<Double>>(hashmap.values())).get(item);
                        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + String.valueOf(arr.get(0)) +
                                "," + String.valueOf(arr.get(1)));
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        startActivity(mapIntent);
                    }
                });
                final AlertDialog alert = builder.create();
                alert.setOnShowListener(new DialogInterface.OnShowListener() {
                    public void onShow(DialogInterface dialog) {
                        ListView lv = alert.getListView();
                        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                                // on long press, delete the key value pair from the hashmap
                                String str = (new ArrayList<String>(hashmap.keySet())).get(position);
                                hashmap.remove(str);
                                Toast toast = Toast.makeText(MainActivity.this, "Deleted Location From Favorites",
                                        Toast.LENGTH_SHORT);
                                toast.show();
                                return true;
                            }
                        });
                    }
                });
                if (!hashmap.isEmpty()) {
                    alert.show();
                }
                else {
                    Toast toast = Toast.makeText(MainActivity.this, "No Saved Locations", Toast.LENGTH_SHORT);
                    toast.show();
                }

            }
        });
    }
  
    double roundThreeDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.###");
        return Double.valueOf(twoDForm.format(d));
    }

    @Override
    protected void onResume() {
      super.onResume();
        if (checkPermissions()) {
            startLocationUpdates();
        } else {
            requestPermissions();
        }
    }
    
    /* Remove the location updates when Activity is paused */
    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    public void onLocationChanged() {
        // Update the UI with the latitude and longitude coordinates
        double lat;
        double longt;
        boolean west = false;
        boolean south = false;
        TextView latText= (TextView)findViewById(R.id.TextView01);
        TextView longText = (TextView)findViewById(R.id.TextView02);
            
        lat=mCurrentLocation.getLatitude();
        if (lat < 0)
        {
            // southern hemisphere
            south = true;
        }
        longt=mCurrentLocation.getLongitude();
        if (longt < 0)
        {
            // western hemisphere
            west = true;
        }
        lat=roundThreeDecimals(lat);
        longt=roundThreeDecimals(longt);

        if (south)
        {
            latText.setText("Latitude:   " + String.valueOf(Math.abs(lat)) + "° S");
        }
        else
        {
            latText.setText("Latitude:   " + String.valueOf(lat) + "° N");
        }
        if (west)
        {
            longText.setText("Longitude: " + String.valueOf(Math.abs(longt)) + "° W");
        }
        else
        {
            latText.setText("Longitude:   " + String.valueOf(longt) + "° E");
        }
    }

		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
		}

	    /**
	     * Given a ArrayList of coordinates, we need to flatten them into an array of
	     * ints before we can stuff them into a map for flattening and storage.
	     * 
	     * @param cvec : a ArrayList of Coordinate objects
	     * @return : a simple array containing the x/y values of the coordinates
	     * as [x1,y1,x2,y2,x3,y3...]
	     */
	    private double[] coordArrayListToArray(ArrayList<Coordinate> cvec) {
	        int count = cvec.size();
	        double [] rawArray = new double[count * 2];
	        for (int i = 0; i < count; i++) {
	            Coordinate c = cvec.get(i);
	            rawArray[2 * i] = c.x;
	            rawArray[2 * i + 1] = c.y;
	        }
	        return rawArray;
	    }
	    
	    /**
	     * Save game state so that the user does not lose anything
	     * if the game process is killed while we are in the 
	     * background.
	     * 
	     * @return a Bundle with this view's state
	     */
        @Override
	    public void onSaveInstanceState(Bundle savedInstanceState) {
	        super.onSaveInstanceState(savedInstanceState);
            File file = new File(getDir("data", MODE_PRIVATE), "hashmap");
            try {
                ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
                outputStream.writeObject(hashmap);
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                Toast toast = Toast.makeText(this, "Error writing writing saved locations", Toast.LENGTH_SHORT);
                toast.show();
            }
	    }
            
	    /**
	     * Given a flattened array of ordinate pairs, we reconstitute them into a
	     * ArrayList of Coordinate objects
	     * 
	     * @param rawArray : [x1,y1,x2,y2,...]
	     * @return a ArrayList of Coordinates
	     */
	    private ArrayList<Coordinate> coordArrayToArrayList(double[] rawArray) {
	        ArrayList<Coordinate> coordArrayList = new ArrayList<Coordinate>();

	        int coordCount = rawArray.length;
	        for (int i = 0; i < coordCount; i += 2) {
	            Coordinate c = new Coordinate(rawArray[i], rawArray[i + 1]);
	            coordArrayList.add(c);
	        }
	        return coordArrayList;
	    }

        @Override
	    public void onRestoreInstanceState(Bundle savedInstanceState) {
	        super.onRestoreInstanceState(savedInstanceState);
            File file = new File(getDir("data", MODE_PRIVATE), "hashmap");
            try {
                ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));
                try {
                    hashmap = (LinkedHashMap) inputStream.readObject();
                } catch (ClassNotFoundException e1)
                {

                }
                inputStream.close();
            } catch (IOException e) {


            }
        }
		
    private class Coordinate implements Serializable {
        public double x;
        public double y;

        public Coordinate(double newX, double newY) {
	            x = newX;
	            y = newY;
	        }

	        public boolean equals(Coordinate other) {
                return x == other.x && y == other.y;
            }

	        @Override
	        public String toString() {
	            return "Coordinate: [" + x + "," + y + "]";
	        }
	    }
}
