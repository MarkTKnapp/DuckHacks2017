package io.github.marktknapp.manhunt;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.location.LocationListener;
import android.location.LocationListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import static io.github.marktknapp.manhunt.CreateGameActivity.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class GameMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private int mkCnt = 0;
    private Marker[] mkList = new Marker[4];
    //private Marker mkJail = new Marker();
    private LatLng curLatLng = null;
    private int mkArrPos = 3;
    private Polygon rect;
    private Marker jail;
    private Button finish_button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        finish_button = (Button) findViewById(R.id.finish_create_button);
        finish_button.setVisibility(View.INVISIBLE);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mkList[0] = null;
        mkList[1] = null;
        mkList[2] = null;
        mkList[3] = null;
       // mkJail = null;
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        findViewById(R.id.finish_create_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ServerConnection().execute("#init#" + lCode.substring(1) + "#a#" + uName + "#" + mkList[0].getPosition().latitude + "#" + mkList[0].getPosition().longitude +
                        "#" + mkList[1].getPosition().latitude + "#" + mkList[1].getPosition().longitude + "#" + mkList[2].getPosition().latitude +
                        "#" + mkList[2].getPosition().longitude + "#" + mkList[3].getPosition().latitude + "#" + mkList[3].getPosition().longitude +
                        "#" + team + "#" + jail.getPosition().latitude + "#" + jail.getPosition().longitude + "#1#1#1#1#1#1#" + time);
            }
        });


    }

    public class ServerConnection extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... params){

            /*out.println("#init#" + lCode.substring(1) + "#a#" + uName + "#" + mkList[0].getPosition().latitude + "#" + mkList[0].getPosition().longitude +
                    "#" + mkList[1].getPosition().latitude + "#" + mkList[1].getPosition().longitude + "#" + mkList[2].getPosition().latitude +
                    "#" + mkList[2].getPosition().longitude + "#" + mkList[3].getPosition().latitude + "#" + mkList[3].getPosition().longitude +
                    "#" + team + "#" + jail.getPosition().latitude + "#" + jail.getPosition().longitude + "#1#1#1#1#1#1#" + time);*/
            out.println(params[0]);
            try {
                String res = in.readLine();
                if (res.startsWith("#fail")) {
                    System.out.println("shits fucked yo");
                } else {
                    System.out.println("we eatin");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(GameMapActivity.this, TeamScreenActivity.class);
            intent.putExtra("userType", "host");
            startActivity(intent);

            return null;
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapStyle(new MapStyleOptions(getResources().getString(R.string.style_json)));

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                //makeUseOfNewLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        LatLng us = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        //mMap.addMarker(new MarkerOptions().position(us).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(us));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(us, 17.0f));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(mkCnt < 4) {
                    Marker curr = mMap.addMarker(new MarkerOptions().position(latLng).title("Marker").draggable(true));
                    mkCnt++;
                    if(mkList[0] == null){
                        mkList[0] = curr;
                    }else if(mkList[1] == null){
                        mkList[1] = curr;
                    }else if(mkList[2] == null){
                        mkList[2] = curr;
                    }else if(mkList[3] == null){
                        mkList[3] = curr;
                    }
                }
                else if(mkCnt == 4){
                    mkCnt++;
                    jail = mMap.addMarker(new MarkerOptions().position(latLng).title("Jail").draggable(true).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                }
                if(mkCnt == 4) {
                    if(rect != null){
                        rect.remove();
                        //redraw markers
                    }
                    rect = mMap.addPolygon(new PolygonOptions().clickable(false).strokeColor(Color.rgb(52,152,219)).fillColor(Color.argb(25,52,152,219)).add(mkList[0].getPosition(), mkList[1].getPosition(), mkList[2].getPosition(), mkList[3].getPosition()));
                }

                if (mkCnt == 5) {
                    finish_button.setVisibility(View.VISIBLE);
                }
            }
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                if(mkList[0].equals(marker)){
                    mkArrPos = 0;
                }else if(mkList[1].equals(marker)){
                    mkArrPos = 1;
                }else if(mkList[2].equals(marker)){
                    mkArrPos = 2;
                }else if(mkList[3].equals(marker)){
                    mkArrPos = 3;
                }else{
                    mkArrPos = -1;
                }
            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                if (mkArrPos != -1) mkList[mkArrPos] = marker;
                if(mkCnt >= 4){
                    if(rect != null){
                        rect.remove();
                        //redraw markers
                    }
                    rect = mMap.addPolygon(new PolygonOptions().clickable(true).strokeColor(Color.rgb(52,152,219)).fillColor(Color.argb(25,52,152,219)).add(mkList[0].getPosition(), mkList[1].getPosition(), mkList[2].getPosition(), mkList[3].getPosition()));
                }
            }
        });

    }


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("GameMap Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
