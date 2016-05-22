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

        ParkMeApplication app = (ParkMeApplication) getApplication();
        mSocket = app.getSocket();

        TextView tv = (TextView)findViewById(R.id.textView7);
        System.out.println(" --------------------------------------------------------- socket eka heduwa");

        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on("new-client", onNewMessage);
        mSocket.connect();
        tv.setText(text);

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

    private Emitter.Listener onNewMessage = new Emitter.Listener() {

        @Override
        public void call(final Object... args) {
            JSONObject obj = (JSONObject)args[0];
              System.out.println(" >>>>>>>>>>>> socket eka ethule --- "+ obj.toString());
            try {
                text = obj.getString("massage");
                System.out.println(" >>>>>>>>>>>> "+ text);
            } catch (JSONException e) {
                return;
            }
        }
    };

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            System.out.println(" >>>>>>>>>>>> socket eka ethule conncet --- "+ args.length);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.off(Socket.EVENT_CONNECT, onNewMessage);
    }

}
