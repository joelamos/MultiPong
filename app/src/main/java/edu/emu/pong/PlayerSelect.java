package edu.emu.pong;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

public class PlayerSelect extends AppCompatActivity implements View.OnClickListener{

    private Button onePButton;
    private Button twoPButton;
    private Button threePButton;
    private Button fourPButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_select);

        onePButton = (Button) findViewById(R.id.onePButton);
        twoPButton = (Button) findViewById(R.id.twoPButton);
        threePButton = (Button) findViewById(R.id.threePButton);
        fourPButton = (Button) findViewById(R.id.fourPButton);

        onePButton.setOnClickListener(this);
        twoPButton.setOnClickListener(this);
        threePButton.setOnClickListener(this);
        fourPButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        startActivity(new Intent(this, MainActivity.class));
    }
}
