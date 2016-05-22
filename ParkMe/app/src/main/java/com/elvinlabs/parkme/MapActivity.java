package com.elvinlabs.parkme;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class MapActivity extends ActionBarActivity {

    private GoogleMap mMap;
    private Socket mSocket;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        ///////////////////////////////////////////////////////////////////////////////////////////////////

        ParkMeApplication app = (ParkMeApplication) getApplication();
        mSocket = app.getSocket();

//        TextView tv = (TextView)findViewById(R.id.textView7);
        System.out.println(" --------------------------------------------------------- socket is created");

        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on("new-client", onNewMessage);
        mSocket.connect();

        ///////////////////////////////////////////////////////////////////////////////////////////////////


        setUpMapIfNeeded();
    }

    private Emitter.Listener onNewMessage = new Emitter.Listener() {

        @Override
        public void call(final Object... args) {
            JSONObject obj = (JSONObject)args[0];
            System.out.println(" >>>>>>>>>>>> in the emmitter method --- "+ obj.toString());
            try {
                name = obj.getString("massage");
                System.out.println(" >>>>>>>>>>>> "+ name);
            } catch (JSONException e) {
                return;
            }
        }
    };

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            System.out.println(" >>>>>>>>>>>> socket.io connceted --- "+ args.length);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.off(Socket.EVENT_CONNECT, onNewMessage);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent i = new Intent(MapActivity.this, SettingsActivity.class);
            startActivity(i);
            finish();
            return true;
        }else if(id == R.id.action_help) {
            Intent i = new Intent(MapActivity.this, HelpActivity.class);
            startActivity(i);
            finish();
            return true;
        }else if(id == R.id.action_about) {
            Intent i = new Intent(MapActivity.this, AboutActivity.class);
            startActivity(i);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        LatLng sydney = new LatLng(7.2566, 80.5966);

        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 14));

        mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.two))
                .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                .title("University of Peradeniya parking")
                .snippet(name+" sample")
                .position(new LatLng(7.25, 80.59)));

        mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.three))
                .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                .title("University of Peradeniya parking")
                .snippet("Available parking slots - 3/10")
                .position(new LatLng(7.2566, 80.5966)));

    }
}
