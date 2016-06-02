package com.mayuresh.sony.potholedetection;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Modules.DirectionFinder;
import Modules.DirectionFinderListener;
import Modules.Route;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DirectionFinderListener {

    private GoogleMap mMap;
    private Button btnFindPath;
    private EditText etOrigin;
    private EditText etDestination;
    private TextView summary;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    private String link = "http://projectnoob.site88.net/retrieve.php?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnFindPath = (Button) findViewById(R.id.btnFindPath);
        etOrigin = (EditText) findViewById(R.id.etOrigin);
        etDestination = (EditText) findViewById(R.id.etDestination);
        summary = (TextView) findViewById(R.id.summary);


        btnFindPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest();
                try{
                    String origin    = URLEncoder.encode(etOrigin.getText().toString(), "UTF-8");
                    String destination  = URLEncoder.encode(etDestination.getText().toString(), "UTF-8");

                    link = "http://projectnoob.site88.net/retrieve.php?origin="+origin+"&destination="+destination;

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                new RetrieveTask().execute();

                summary.setText("The route "+etOrigin.getText()+"to " +etDestination.getText().toString()+"has x potholes. The score is 'x'");
            }
        });

    }


    private class RetrieveTask extends AsyncTask<Void, Void, String> {




        @Override
        protected String doInBackground(Void... params) {
            Log.d("max", "in Retrieve Task");
            URL url = null;
            StringBuffer sb = new StringBuffer();
            try {
                Log.d("max", "in try");
//                String link = "http://projectnoob.site88.net/retrieveold.php";
                url = new URL(link);
                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);
                Log.d("max", "setup");
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                Log.d("max", "set read");
                String line = null;
                // Read Server Response
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                    break;
                }
                Log.d("max", "end Try connect");

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("max", sb.toString());
            return sb.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("max", "in onPost retrieve");

            super.onPostExecute(result);
            new ParserTask().execute(result);

            Log.d("max", "end onpost Retreive");
        }

    }

    private class ParserTask extends AsyncTask<String, Void, List<HashMap<String, String>>>{
        @Override
        protected List<HashMap<String, String>> doInBackground(String... params) {
            Log.d("max", "in Parser Task");

            MarkerJSONParser markerParser = new MarkerJSONParser();
            JSONObject json = null;
            try {
                json = new JSONObject(params[0]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            List<HashMap<String, String>> markersList = markerParser.parse(json);
            Log.d("max", "end Parser Task");
            return markersList;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {
            Log.d("max", "in onPost Parser");
            for(int i=0; i<result.size();i++){
                HashMap<String, String> marker = result.get(i);
                LatLng latlng = new LatLng(Double.parseDouble(marker.get("lat")), Double.parseDouble(marker.get("lng")));
                addMarker(latlng);
            }
            Log.d("max", "end onPost Parser");
        }
    }


    private void addMarker(LatLng latlng) {
        Log.d("max", "in addMarker");

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latlng);
        markerOptions.title(latlng.latitude + "," + latlng.longitude);
        mMap.addMarker(markerOptions)
                .setIcon(BitmapDescriptorFactory.fromResource(R.drawable.small_red_dot));

//        if(marker1!= null) {
//
//            PolylineOptions options = new PolylineOptions()
//                    .add(marker1.getPosition())
//                    .add(markerOptions.getPosition());
//
//            mMap.addPolyline(options);
//        }
//        marker1 = markerOptions;
        Log.d("max", "end addMarker");
    }



    private void sendRequest() {
        String origin = etOrigin.getText().toString();
        String destination = etDestination.getText().toString();
        if (origin.isEmpty()) {
            Toast.makeText(this, "Please enter origin address!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            new DirectionFinder(this, origin, destination).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng test = new LatLng(18.455300, 73.791474);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(test, 18));
        originMarkers.add(mMap.addMarker(new MarkerOptions()
                .title("Test")
                .position(test)));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
    }


    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding direction..!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
            ((TextView) findViewById(R.id.tvDuration)).setText(route.duration.text);
            ((TextView) findViewById(R.id.tvDistance)).setText(route.distance.text);

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
                    .title(route.startAddress)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green))
                    .title(route.endAddress)
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(10);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
    }
}
