package io.github.marktknapp.manhunt;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class CreateGameActivity extends AppCompatActivity {
    public static Socket socket;
    public static PrintWriter out;
    public static BufferedReader in;
    public static final String addr = "155.246.202.60";
    public static String lCode;
    public static String uName;
    public static int time;
    public static String team;
    public static String gameType;
    public static double[] bounds;
    public static double[] jail;
    public static int runnerCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_game2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Spinner spinner = (Spinner) findViewById(R.id.game_types);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.game_types_arr, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        Intent intent = getIntent();
        lCode = intent.getStringExtra("lCode");

        TextView lCodeText = (TextView) findViewById(R.id.lCodeTextView);
        lCodeText.setText(lCode.toUpperCase().substring(1));

        final RadioButton rad_hunters = (RadioButton) findViewById(R.id.rad_button_hunters);
        final RadioButton rad_runners = (RadioButton) findViewById(R.id.rad_button_runners);

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

        findViewById(R.id.next_create_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateGameActivity.this, GameMapActivity.class);

                final EditText gameTimeText = (EditText) findViewById(R.id.text_game_time);
                if(gameTimeText.getText().toString().equals(null) || gameTimeText.getText().toString().equals("")){
                    Context context1 = getApplicationContext();
                    CharSequence text1 = "Must enter a time!";
                    int duration1 = Toast.LENGTH_SHORT;
                    Toast toast1 = Toast.makeText(context1, text1, duration1);
                    toast1.show();
                    return;
                }
                time = Integer.parseInt(gameTimeText.getText().toString());

                gameType = "NOT RELEVANT";

                intent.putExtra("lCode", lCode);
                intent.putExtra("uName", uName);
                intent.putExtra("team", team);
                intent.putExtra("time", time);
                intent.putExtra("gameType", gameType);

                startActivity(intent);
            }
        });
    }


}
