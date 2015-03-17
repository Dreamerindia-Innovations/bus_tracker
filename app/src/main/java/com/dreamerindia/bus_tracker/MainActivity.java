package com.dreamerindia.bus_tracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Display;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

public class MainActivity extends FragmentActivity {

    GoogleMap map;
    ArrayList<LatLng> markerPoints;
    Handler handler = null;
    Runnable runnable = null;
    TextView ll, myll;
    LocationManager mlocManager;
    LocationListener mlocListener;
    private double sourceLatitude, sourceLongitude;
    private int width, height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!isNetworkConnected()) {
            Toast.makeText(getApplicationContext(), "Please Enable Internet connection", Toast.LENGTH_LONG).show();
        }
        if (!isGooglePlayServicesAvailable()) {
            showSettingsAlert();
        }
        getScreenDimensions();
        ll = (TextView) findViewById(R.id.ll);
        myll = (TextView) findViewById(R.id.myll);
        markerPoints = new ArrayList<LatLng>();

        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        map = fm.getMap();
        map.setMyLocationEnabled(true);
        mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mlocListener = new MyLocationListener();

        LatLng ll = new LatLng(21.0000, 78.0000);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 3);
        map.moveCamera(update);

        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 0,
                mlocListener);
        mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                10, 0, mlocListener);

        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                update();
                handler.postDelayed(runnable, 10000);
            }
        };
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 10000);
    }

    private void getScreenDimensions() {
        Display display = getWindowManager().getDefaultDisplay();
        width = display.getWidth();
        height = display.getHeight();
    }

    private void update() {
        new Thread(new Runnable() {
            public void run() {
                {
                    new GetLocationFromUrl().execute("http://www.embeddedcollege.org/rewebservices/mylocation.txt");
                }
            }
        }).start();
    }


    @Override
    protected void onResume() {
        super.onResume();
        mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                10, 0, mlocListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mlocManager.removeUpdates(mlocListener);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            // There are no active networks.
            Toast.makeText(getApplicationContext(), "Please Enable Internet connection", Toast.LENGTH_LONG).show();
            return false;
        } else
            return true;
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new
                AlertDialog.Builder(
                MainActivity.this);
        alertDialog.setTitle("SETTINGS");
        alertDialog.setMessage("Enable Location Provider! Go to settings menu?");
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        MainActivity.this.startActivity(intent);
                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

    private void sourceTodestination() {
//        sourceLongitude = 79.495314;
//        sourceLatitude = 11.948664;
        LatLng point = new LatLng(sourceLatitude, sourceLongitude);
        String temp = ll.getText().toString();
        if (temp.equals("")) {
            Toast.makeText(getApplicationContext(), "Bus location not received", Toast.LENGTH_SHORT).show();
        } else {
            StringTokenizer tokens = new StringTokenizer(temp, ",");
            String first = tokens.nextToken();
            String second = tokens.nextToken();
            double toLat = Double.parseDouble(first);
            double toLng = Double.parseDouble(second);
            LatLng point1 = new LatLng(toLat, toLng);
            CameraUpdate center =
                    CameraUpdateFactory.newLatLng(new LatLng(toLat, toLng));
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(10);

            map.moveCamera(center);
            map.animateCamera(zoom);

            markerPoints.add(point);
            markerPoints.add(point1);

            MarkerOptions options = new MarkerOptions();

            options.position(point);
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

            MarkerOptions options1 = new MarkerOptions();
            options1.position(point1);
            options1.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

            map.addMarker(options);
            map.addMarker(options1);

            if (markerPoints.size() >= 2) {
                LatLng origin = markerPoints.get(0);
                LatLng dest = markerPoints.get(1);

                String url = getDirectionsUrl(origin, dest);
                DownloadTask downloadTask = new DownloadTask();
                downloadTask.execute(url);
            }
        }
    }

    private void sourceDestination() {
        String temp = ll.getText().toString();
        if (temp.equals("")) {
            Toast.makeText(getApplicationContext(), "Bus location not received", Toast.LENGTH_SHORT).show();
        } else {
            StringTokenizer tokens = new StringTokenizer(temp, ",");
            String first = tokens.nextToken();
            String second = tokens.nextToken();
            double toLat = Double.parseDouble(first);
            double toLng = Double.parseDouble(second);
            if (toLat == 0 && toLng == 0) {
                Toast.makeText(getApplicationContext(), "Bus location unidentifiable", Toast.LENGTH_SHORT).show();
            } else {
                LatLng point = new LatLng(toLat, toLng);
                CameraUpdate center =
                        CameraUpdateFactory.newLatLng(new LatLng(toLat, toLng));
                CameraUpdate zoom = CameraUpdateFactory.zoomTo(10);

                map.moveCamera(center);
                map.animateCamera(zoom);

                markerPoints.add(point);

                MarkerOptions options = new MarkerOptions();

                options.position(point);
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                map.addMarker(options);
            }
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception while downloading url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            parserTask.execute(result);

        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }
                lineOptions.addAll(points);
                lineOptions.width(2);
                lineOptions.color(Color.RED);
            }
            map.addPolyline(lineOptions);
        }
    }

    /* Class My Location Listener */
    public class MyLocationListener implements LocationListener {

        public void onLocationChanged(Location loc) {
            sourceLatitude = loc.getLatitude();
            sourceLongitude = loc.getLongitude();
            sourceTodestination();
            Toast.makeText(getApplicationContext(), sourceLatitude + " - " + sourceLongitude,
                    Toast.LENGTH_SHORT).show();
        }

        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(), "Gps Disabled",
                    Toast.LENGTH_SHORT).show();
        }

        public void onProviderEnabled(String provider) {
            android.util.Log.v("", "Latitud = ");
            Toast.makeText(getApplicationContext(), "Gps Enabled",
                    Toast.LENGTH_SHORT).show();
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            android.util.Log.v("", "status = ");
        }
    }

    private class GetLocationFromUrl extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(params[0]);
                HttpResponse response = httpClient.execute(httpGet);
                HttpEntity entity = response.getEntity();

                BufferedHttpEntity buf = new BufferedHttpEntity(entity);

                InputStream is = buf.getContent();

                BufferedReader r = new BufferedReader(new InputStreamReader(is));

                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line + "\n");
                }
                String result = total.toString();
                Log.i("Get URL", "Downloaded string: " + result);
                return result;
            } catch (Exception e) {
                Log.e("Get Url", "Error in downloading: " + e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ll.setText(result);
            sourceDestination();
        }
    }

}
