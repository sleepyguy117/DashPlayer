package com.example.hans.dashplayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {


    private Button playButton;

    private Button playButton2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        playButton = findViewById(R.id.playButton);
        playButton2 = findViewById(R.id.playButton2);


        playButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
            startActivity(intent);
        });


        playButton2.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PlayerActivity2.class);
            startActivity(intent);
        });
    }
}
