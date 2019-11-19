package com.example.callmama;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }
    /*
    *  this method for initing the layout of the main activity
    * */
    private void init(){
        Button mapBtn = findViewById(R.id.btnMap);
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openMapIntent = new Intent(MainActivity.this,MapActivity.class);
                startActivity(openMapIntent);
            }
        });

    }
    //TODO: implement check service method to check latest version of the map & the needed fies to run the app
}
