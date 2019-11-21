package com.example.callmama;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

//TODOS
// TODO: add search bar for the user to search locations
// TODO: add avilabilty for the user to add marker in the location he wish to go
// TODO: add polyline to follow the path of the user activity
// TODO: add geofence to monitior the user's location


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
