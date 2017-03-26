package io.github.marktknapp.manhunt;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.vision.text.Text;

import static io.github.marktknapp.manhunt.CreateGameActivity.lCode;
import static io.github.marktknapp.manhunt.CreateGameActivity.team;
import static io.github.marktknapp.manhunt.CreateGameActivity.time;
import static io.github.marktknapp.manhunt.CreateGameActivity.uName;

public class GameOverActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        TextView text = (TextView) findViewById(R.id.winnertext);
        text.setText(getIntent().getStringExtra("winner"));

        findViewById(R.id.back_dash).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GameOverActivity.this, DashboardActivity.class);
                startActivity(intent);
            }
        });
    }
}
