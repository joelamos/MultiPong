package edu.emu.pong;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;


public class HomeScreen extends AppCompatActivity implements View.OnClickListener {

    private TextView multiPongTV;
    private Button createButton;
    private Button joinButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        multiPongTV = (TextView) findViewById(R.id.multiPongTV);
        createButton = (Button) findViewById(R.id.createButton);
        joinButton = (Button) findViewById(R.id.joinButton);

        createButton.setOnClickListener(this);
        joinButton.setOnClickListener(this);


    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.createButton:
                startActivity(new Intent(this, PlayerSelect.class));
                break;
            case R.id.joinButton:
                startActivity(new Intent(this, MainActivity.class));
                break;
        }
    }
}
