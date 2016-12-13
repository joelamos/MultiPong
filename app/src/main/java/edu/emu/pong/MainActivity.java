package edu.emu.pong;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends Activity {

    private PongView pongView;
    private TextView lostBallsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pongView = (PongView) findViewById(R.id.pong_view);
        lostBallsView = (TextView) findViewById(R.id.BallsMissedint);

    }

    @Override
    public void onBackPressed(){
        finish();
        super.onBackPressed();
    }

    public void setLostBalls(int lostBalls) {
        lostBallsView.setText(lostBalls + "");
    }
}