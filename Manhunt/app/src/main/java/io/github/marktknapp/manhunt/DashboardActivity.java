package io.github.marktknapp.manhunt;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static android.provider.AlarmClock.EXTRA_MESSAGE;
import static io.github.marktknapp.manhunt.CreateGameActivity.*;

public class DashboardActivity extends AppCompatActivity {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private String canJoin = "";
    boolean awaitFinish = false;
    boolean errored = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        findViewById(R.id.createbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.editText);
                if(editText.getText().toString().equals(null) || editText.getText().toString().equals("")){
                    Context context1 = getApplicationContext();
                    CharSequence text1 = "Must enter a Nickname!";
                    int duration1 = Toast.LENGTH_SHORT;
                    Toast toast1 = Toast.makeText(context1, text1, duration1);
                    toast1.show();
                }else {
                    uName = editText.getText().toString();
                    new ServerConnection().execute("host");
                }

            }
        });

        findViewById(R.id.joinbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.editText);
                uName = editText.getText().toString();
                EditText joinText = (EditText) findViewById(R.id.jointext);
                lCode = "#" + joinText.getText().toString();
                new ServerConnection().execute("player");
                while (!awaitFinish){}
                if (errored){
                    Context context = getApplicationContext();
                    CharSequence text = "No Lobby or false lobby code";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                    awaitFinish = false;
                    errored = false;
                }

            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        final RadioButton rad_hunters = (RadioButton) findViewById(R.id.hunterButton);
        final RadioButton rad_runners = (RadioButton) findViewById(R.id.runnerButton);

        team = "hunter";

        rad_hunters.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    rad_runners.setChecked(false);
                    team = "hunter";
                }
            }
        });

        rad_runners.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    rad_hunters.setChecked(false);
                    team = "runner";
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        return true;
    }

    private class ServerConnection extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... params) {
            try {
                if(params[0].equals("host")){
                    socket = new Socket(addr, 9898);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = new PrintWriter(socket.getOutputStream(), true);
                    out.println("#getlcode");
                    lCode = in.readLine();

                    Intent intent = new Intent(DashboardActivity.this, CreateGameActivity.class);
                    intent.putExtra("lCode", lCode);
                    startActivity(intent);
                }else{
                    socket = new Socket(addr, 9898);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = new PrintWriter(socket.getOutputStream(), true);
                    out.println("#join#" + lCode.substring(1) + "#a#" + uName + "#" + team);
                    canJoin = in.readLine();
                    if(canJoin.startsWith("#fail")){
                        errored = true;
                        awaitFinish = true;
                        return null;
                    }else{
                        awaitFinish = true;
                        Log.println(Log.WARN, "TEST:", "THIS SUCKS");
                        Intent intent = new Intent(DashboardActivity.this, TeamScreenActivity.class);
                        intent.putExtra("lCode", lCode);
                        intent.putExtra("userType", "join");
                        startActivity(intent);
                    }
                }

            }
            catch (IOException e){

            }
            return null;
        }
    }
}
