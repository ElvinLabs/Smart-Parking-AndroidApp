package com.elvinlabs.parkme;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import android.util.Log;

public class HelpActivity extends ActionBarActivity {

    private Socket mSocket;
    private String text = "test";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        Button clickButton = (Button) findViewById(R.id.back);
        clickButton.setOnClickListener( new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HelpActivity.this, MapActivity.class);
                startActivity(i);
                finish();
            }
        });

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
