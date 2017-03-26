package io.github.marktknapp.manhunt;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

import static android.view.View.INVISIBLE;
import static io.github.marktknapp.manhunt.CreateGameActivity.bounds;
import static io.github.marktknapp.manhunt.CreateGameActivity.in;
import static io.github.marktknapp.manhunt.CreateGameActivity.jail;
import static io.github.marktknapp.manhunt.CreateGameActivity.lCode;
import static io.github.marktknapp.manhunt.CreateGameActivity.out;
import static io.github.marktknapp.manhunt.CreateGameActivity.runnerCount;
import static io.github.marktknapp.manhunt.CreateGameActivity.time;

public class TeamScreenActivity extends AppCompatActivity {
    public String[] hunters;
    public String[] runners;
    public String teamInfo;
    boolean setTeams = false;
    boolean doStart = false;
    Activity thisAct = this;
    boolean isHost = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ServerConnection(thisAct).execute("Begin");
                Intent newIntent = new Intent(TeamScreenActivity.this, RunningGameMapActivity.class);
                newIntent.putExtra("userType", "host");
                startActivity(newIntent);
            }
        });
        Intent intent = getIntent();
        if(intent.getStringExtra("userType").equals("host")){
            isHost = true;
            new ServerConnection(this).execute("host");
            TextView teamView = (TextView) findViewById(R.id.teams);
            String hunterList = "";
            String runnerList = "";
            while(!setTeams) {}
            for(int i = 1; i < hunters.length; i++){
                hunterList += hunters[i] + "\n";
            }

            for(int i = 1; i < runners.length; i++){
                runnerList += runners[i] + "\n";
            }

            teamView.setText("Hunters:\n" + hunterList + "\n\n" + "Runners:\n" + runnerList);

        }else{
            fab.setVisibility(INVISIBLE);
            new ServerConnection(this).execute("player");
            TextView teamView = (TextView) findViewById(R.id.teams);
            String hunterList = "";
            String runnerList = "";
            while(!setTeams) {}
            for(int i = 1; i < hunters.length; i++){
                hunterList += hunters[i] + "\n";
            }

            for(int i = 1; i < runners.length; i++){
                runnerList += runners[i] + "\n";
            }

            teamView.setText("Hunters:\n" + hunterList + "\n\n" + "Runners:\n" + runnerList);

            /*while(!doStart){}
            Intent newIntent = new Intent(TeamScreenActivity.this, RunningGameMapActivity.class);
            newIntent.putExtra("userType", "player");
            startActivity(newIntent);*/
        }

    }

    public void startGame(){
        Intent newIntent = new Intent(TeamScreenActivity.this, RunningGameMapActivity.class);
        newIntent.putExtra("userType", "player");
        startActivity(newIntent);
    }

    public class ServerConnection extends AsyncTask<String, Void, String>{
        private Activity activity;

        public ServerConnection(Activity activity){
            this.activity = activity;
        }

        @Override
        protected String doInBackground(String... params){
            if (params.length == 1 && params[0].equals("Begin")){
                try {
                    out.println("#start#" + lCode.substring(1) + "#runner");
                    String result = in.readLine();
                    Log.println(Log.WARN, "Count:", result);
                    if (result.startsWith("#start")){
                        runnerCount = Integer.parseInt(result.split("#")[3]);
                        doStart = true;
                    }
                }
                catch (IOException e){

                }
            }
            else {
                try {
                    out.println("#refresh#" + lCode.substring(1) + "#hunter#runner");
                    teamInfo = in.readLine();
                    Log.println(Log.WARN, "TeamInfo:", teamInfo);
                } catch (IOException e) {

                }

                hunters = teamInfo.split("#")[1].split(",");
                runners = teamInfo.split("#")[2].split(",");

                setTeams = true;

                try {
                    out.println("#getbounds#" + lCode.substring(1));

                    String result = in.readLine();
                    Log.println(Log.WARN, "Result:", result);
                    String[] resultArr = result.split("#");

                    bounds = new double[8];
                    bounds[0] = Double.parseDouble(resultArr[9]);
                    bounds[1] = Double.parseDouble(resultArr[10]);
                    bounds[2] = Double.parseDouble(resultArr[11]);
                    bounds[3] = Double.parseDouble(resultArr[12]);
                    bounds[4] = Double.parseDouble(resultArr[13]);
                    bounds[5] = Double.parseDouble(resultArr[14]);
                    bounds[6] = Double.parseDouble(resultArr[15]);
                    bounds[7] = Double.parseDouble(resultArr[16]);

                    jail = new double[2];
                    jail[0] = Double.parseDouble(resultArr[1]);
                    jail[1] = Double.parseDouble(resultArr[12]);
                    time = Integer.parseInt(resultArr[17]);

                    if (params[0].equals("host")){
                        return null;
                    }else{
                        String startCommand = in.readLine();
                        Log.println(Log.WARN, "GotCommand:", startCommand);
                        if (startCommand.startsWith("#start")){
                            runnerCount = Integer.parseInt(startCommand.split("#")[3]);
                            ((TeamScreenActivity)activity).startGame();
                        }
                    }
                } catch (IOException e) {

                }
            }
            return null;
        }
    }
}
