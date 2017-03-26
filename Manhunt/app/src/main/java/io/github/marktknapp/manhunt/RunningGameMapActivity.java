package io.github.marktknapp.manhunt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.ReceiverCallNotAllowedException;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.vision.text.Text;

import java.io.IOException;
import java.net.SocketException;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static io.github.marktknapp.manhunt.CreateGameActivity.bounds;
import static io.github.marktknapp.manhunt.CreateGameActivity.in;
import static io.github.marktknapp.manhunt.CreateGameActivity.jail;
import static io.github.marktknapp.manhunt.CreateGameActivity.lCode;
import static io.github.marktknapp.manhunt.CreateGameActivity.out;
import static io.github.marktknapp.manhunt.CreateGameActivity.runnerCount;
import static io.github.marktknapp.manhunt.CreateGameActivity.socket;
import static io.github.marktknapp.manhunt.CreateGameActivity.team;
import static io.github.marktknapp.manhunt.CreateGameActivity.time;
import static io.github.marktknapp.manhunt.CreateGameActivity.uName;
import static io.github.marktknapp.manhunt.R.id.caught_button;
import static io.github.marktknapp.manhunt.R.id.text_game_time;

public class RunningGameMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    int inJail = 0;
    String response;
    boolean gameRunning = true;
    Marker locationMarker;
    AsyncTask rs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running_game_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        rs = new ReceiveServer(this).execute("");
        final TextView game_time = (TextView) findViewById(R.id.text_game_time);

        updateJailCnt(0,false);

        new CountDownTimer(time * 60000, 1000){
            @Override
            public void onTick(long millisUntilFinished) {
                if (((millisUntilFinished % 60000) / 1000) > 9) {
                    game_time.setText((millisUntilFinished / 60000) + ":" + (millisUntilFinished % 60000) / 1000);
                }else{
                    game_time.setText((millisUntilFinished / 60000) + ":0" + (millisUntilFinished % 60000) / 1000);
                }
            }

            @Override
            public void onFinish() {
                game_time.setText("GAME OVER!");
                gameOver("Runners Won!");
            }
        }.start();

        findViewById(R.id.break_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button brkButt = (Button) findViewById(R.id.break_button);
                if(brkButt.getVisibility() == (View.VISIBLE)){
                    //new SendServer().execute("break");
                    ((ReceiveServer)rs).newMessage = "Break";
                }
            }
        });

        findViewById(R.id.caught_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button butt = (Button) findViewById(R.id.caught_button);
                Button brkBut = (Button) findViewById(R.id.break_button);
                if(butt.getVisibility() == (View.VISIBLE)){
                    butt.setVisibility(View.INVISIBLE);
                    brkBut.setVisibility(View.INVISIBLE);
                }
                //new SendServer().execute("Tag");
                ((ReceiveServer)rs).newMessage = "Tag";
            }
        });
    }

    public void updateJailCnt(int i, final boolean reshow){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final TextView jail_cnt = (TextView) findViewById(R.id.jailCnt);
                jail_cnt.setText("Jail Count: " + inJail);
                if (reshow){
                    Button butt = (Button) findViewById(R.id.caught_button);
                    Button brkBut = (Button) findViewById(R.id.break_button);
                    butt.setVisibility(View.VISIBLE);
                    brkBut.setVisibility(View.VISIBLE);
                }
            }
        });
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

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                LatLng us = new LatLng(location.getLatitude(), location.getLongitude());
                locationMarker.setPosition(us);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(us));

                float[] lats = new float[4];
                float[] longs = new float[4];

                lats[0] = Math.abs((float)bounds[0]);
                lats[1] = Math.abs((float)bounds[2]);
                lats[2] = Math.abs((float)bounds[4]);
                lats[3] = Math.abs((float)bounds[6]);
                longs[0] = Math.abs((float)bounds[1]);
                longs[1] = Math.abs((float)bounds[3]);
                longs[2] = Math.abs((float)bounds[5]);
                longs[3] = Math.abs((float)bounds[7]);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }

        if(team == "hunter") {
            findViewById(R.id.caught_button).setVisibility(INVISIBLE);
            findViewById(R.id.break_button).setVisibility(INVISIBLE);
        }
        else findViewById(R.id.caught_button).setVisibility(View.VISIBLE);



        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        LatLng start = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        locationMarker = mMap.addMarker(new MarkerOptions().position(start).title("You are here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 17.0f));
        mMap.addPolygon(new PolygonOptions().clickable(true).strokeColor(Color.rgb(52,152,219)).fillColor(Color.argb(25,52,152,219)).add
                        (new LatLng(bounds[0], bounds[1]),
                        new LatLng(bounds[2], bounds[3]),
                        new LatLng(bounds[4], bounds[5]),
                        new LatLng(bounds[6], bounds[7])));

        /*while(gameRunning) {
            LatLng us = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            locationMarker.setPosition(us);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(us));
            try {
                Thread.sleep(5000);                 //1000 milliseconds is one second.
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }*/

    }

    public boolean inBounds(int nvert, float[] x, float[] y, float testx, float testy){
        int i, j;
        boolean c = false;
        for (i = 0, j = nvert-1; i < nvert; j = i++) {
            if ( ((y[i]>testy) != (y[j]>testy)) &&
                    (testx < (x[j]-x[i]) * (testy-y[i]) / (y[j]-y[i]) + x[i]) )
                c = !c;
        }
        return c;
    }

    public class SendServer extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... params){
            try {
                Log.println(Log.WARN, "TAG:", "Pre-Tag");
                out.println("#tag#" + lCode.substring(1));
            }catch (Exception e){

            }
            return null;
        }
    }

    public class ReceiveServer extends AsyncTask<String, Void, String>{
        private Activity activity;
        private String newMessage;
        public ReceiveServer(Activity activity){
            this.activity = activity;
            newMessage = "";
        }

        public void addMessage(String message){
            newMessage = message;
        }
        @Override
        protected String doInBackground(String... params){
            try {
                socket.setSoTimeout(50);
            } catch (SocketException e) {
                e.printStackTrace();
            }
            while(true){
               try {
                   if(newMessage != "") {
                       if (newMessage == "Tag")
                        out.println("#tag#" + lCode.substring(1));
                       else if (newMessage == "Break")
                           out.println("#jailbreak#" + lCode.substring(1));

                       newMessage = "";
                   }else{
                        response = in.readLine();
                       if(response.startsWith("#tag")){
                           inJail++;
                           ((RunningGameMapActivity)activity).updateJailCnt(inJail,false);
                           if (inJail >= runnerCount){
                               ((RunningGameMapActivity)activity).gameOver("Hunters Won!");
                           }
                       }
                       else if (response.startsWith("#jailbreak")){
                           inJail= 0;
                           ((RunningGameMapActivity)activity).updateJailCnt(inJail,true);

                       }
                       if (response.startsWith("THOMASISTHEBIGGESTLOSEREVER")) {
                           return null;
                       }
                   }
               }catch(IOException e){

               }

           }
        }
    }

    public void gameOver(String winner){
        Intent intent = new Intent(this, GameOverActivity.class);
        intent.putExtra("winner", winner);
        startActivity(intent);
    }
}
