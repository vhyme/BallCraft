package cn.vhyme.ballcraft;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GameView game = (GameView) findViewById(R.id.game);
        View split = findViewById(R.id.split);
        split.setOnClickListener((v) -> game.split());
    }
}
