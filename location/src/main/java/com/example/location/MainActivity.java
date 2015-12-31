package com.example.location;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.content.DialogInterface;
import android.view.Menu;
import android.widget.EditText;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.text.InputType;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends Activity implements LocationListener{
	 LocationProvider provider;
	 LocationManager locationManager;
	 private  Handler mHandler = new Handler();
	 String providerName;
	 Location locatio;
	 double longt;
	 double  lat;
     private HashMap<String, Coordinate> hashmap = new HashMap<String, Coordinate>();
     private String m_Text = "";

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        
        // Retrieve a list of location providers that have fine accuracy, no monetary cost, etc
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setCostAllowed(false);
      
        providerName = locationManager.getBestProvider(criteria, true);
        try {
            locatio = locationManager.getLastKnownLocation(providerName);
        }
        catch (SecurityException e)
        {
            Toast toast = Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT);
            toast.show();
        }
        // If no suitable provider is found, null is returned.
        if (providerName != null) {
        	Toast.makeText(this, providerName, Toast.LENGTH_SHORT).show();
        }

        Button location= (Button) findViewById(R.id.button1);
        location.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click for location
            	LocationManager locationManager =(LocationManager) getSystemService(Context.LOCATION_SERVICE);
                final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (!gpsEnabled) {
                	enableLocationSettings();
                }
                if (locatio != null) {
                    onLocationChanged(locatio);
                  } 
               onStarter();
            }
        });

        Button map = (Button) findViewById(R.id.button2);
        map.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
              //start the map
              Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:"+lat+","+longt+"?q="+lat+","+longt+"(current Location)"));
              startActivity(intent);
            }
        });

        Button saveLocation = (Button) findViewById(R.id.button3);
        saveLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // add the location to the arraylist

                LocationManager locationManager =(LocationManager) getSystemService(Context.LOCATION_SERVICE);
                final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (!gpsEnabled) {
                    enableLocationSettings();
                }
                if (locatio != null) {
                    onLocationChanged(locatio);
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Save Location");

                // Set up the input
                final EditText input = new EditText(MainActivity.this);
                 // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        m_Text = input.getText().toString();
                        // add the new coordinate to the hashmap
                        hashmap.put(m_Text, new Coordinate(lat, longt));
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
                builder.setTitle("Select Location");
                builder.setItems(savedLocations, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        // start the map to navigate to the location
                        Uri gmmIntentUri = Uri.parse("google.navigation:q="+String.valueOf(lat) +
                                        "," + String.valueOf(longt));
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        startActivity(mapIntent);
                    }
                });
                AlertDialog alert = builder.create();
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

    protected void onStarter(){
        try
        {
    	    locationManager.requestLocationUpdates(providerName, 1000, 0, this);
        }
        catch (SecurityException e) {
            Toast toast = Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT);
            toast.show();
        }
    }
  
    double roundThreeDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.###");
        return Double.valueOf(twoDForm.format(d));
    }

    @Override
    protected void onResume() {
      super.onResume();
        try {
            locationManager.requestLocationUpdates(providerName, 1000, 0, this);
        }
        catch (SecurityException e) {
            Toast toast = Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT);
            toast.show();
        }
    }
    
    /* Remove the locationlistener updates when Activity is paused */
    @Override
    protected void onPause() {
      super.onPause();
        try {
            locationManager.removeUpdates(this);
        }
        catch (SecurityException e) {
            Toast toast = Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private void enableLocationSettings() {
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(settingsIntent);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    public void onLocationChanged(Location location) {
        // Update the UI with the latitude and longitude coordinates

        boolean west = false;
        boolean south = false;
        TextView latText= (TextView)findViewById(R.id.TextView01);
        TextView longText = (TextView)findViewById(R.id.TextView02);
            
        lat=location.getLatitude();
        if (lat < 0)
        {
            // southern hemisphere
            south = true;
        }
        longt=location.getLongitude();
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
			Toast.makeText(this, "Enabled new provider " + providerName,
			       Toast.LENGTH_SHORT).show();

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
            // savedInstanceState.putSerializable("HashMap", hashmap);
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
            // hashmap = (HashMap) savedInstanceState.getSerializable("HashMap");
        }
		
    private class Coordinate {
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
