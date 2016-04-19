package com.mayuresh.sony.potholedetection;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Generate extends AppCompatActivity {


    private Button buttonPath;
    private EditText enterOrigin;
    private EditText enterDestination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate);

        buttonPath = (Button) findViewById(R.id.start);
        enterOrigin = (EditText) findViewById(R.id.enterOrigin);
        enterDestination = (EditText) findViewById(R.id.enterDestination);

        buttonPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent in = new Intent(getApplicationContext(), service.class );
                startService(in);
                Toast.makeText(Generate.this, "Service Started", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStop() {
        Intent  in = new Intent(getApplicationContext(),service.class);
        stopService(in);
        super.onStop();
    }
}

