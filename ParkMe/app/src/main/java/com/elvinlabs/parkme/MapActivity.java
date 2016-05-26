package com.elvinlabs.parkme;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.content.Context;
import android.widget.Toast;
import android.location.Location;
import android.location.LocationManager;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class MapActivity extends ActionBarActivity {

    private GoogleMap mMap;
    private Socket mSocket;
    private Toast toast;
    private boolean isSataliteMode = false;
    private String filtering = "both";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Bundle extras = getIntent().getExtras();
        try{
            isSataliteMode = extras.getBoolean("isSalatite");
            filtering = extras.getString("filter");
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+ extras.getString("filter"));
        }catch(NullPointerException e){
            System.out.println(e);
        }

        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        //if the GPS is not activated this method trigured
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        }

        ParkMeApplication app = (ParkMeApplication) getApplication();
        mSocket = app.getSocket();
        System.out.println(" --------------------------------------------------------- socket is created");

        Context context = getApplicationContext();
        CharSequence text = "CONNECTING TO THE SERVER ...";
        int duration = Toast.LENGTH_LONG;
        toast = Toast.makeText(context, text, duration);
        toast.show();

        // connect to the server through socket.io
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on("new-client", onNewMessage);
        mSocket.on("node-mcu", onNewMessage);
        mSocket.connect();

        setUpMapIfNeeded();
    }

    public void onButtonClicked(View v){
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on("new-client", onNewMessage);
        mSocket.on("node-mcu", onNewMessage);
        mSocket.connect();

        setUpMapIfNeeded();
    }


    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog,  final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }



    /////////////////////////////////////////////////////////////////////////////////////////
    private Emitter.Listener onMessage = new Emitter.Listener() {

        @Override
        public void call(final Object... args) {
            final JSONArray obj = (JSONArray)args[0];

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    System.out.println(" >>>>>>>>>>>> new-client --- "+ obj.length());
                    Context context = getApplicationContext();
                    CharSequence text = "new-client - "+ obj.length();
                    int duration = Toast.LENGTH_LONG;
                    toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            });
        }

    };

    private Emitter.Listener onMessageMCU = new Emitter.Listener() {

        @Override
        public void call(final Object... args) {
            final JSONArray obj = (JSONArray)args[0];

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    System.out.println(" >>>>>>>>>>>> MCU  --- "+ obj.length());
                    Context context = getApplicationContext();
                    CharSequence text = "MCU - "+ obj.length();
                    int duration = Toast.LENGTH_LONG;
                    toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            });
        }

    };
    ///////////////////////////////////////////////////////////////////////////////////////





    private Emitter.Listener onNewMessage = new Emitter.Listener() {

        @Override
        public void call(final Object... args) {
            final JSONArray obj = (JSONArray)args[0];

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMap.clear();
                    for(int i=0; i<obj.length(); i++){
                        try{

                            System.out.println(" >>>>>>>>>>>> socket.io connceted --- "+ obj.getJSONObject(i).toString());

                            if(filtering.equals("indoor") && obj.getJSONObject(i).getString("prkType").equals("Indoor")){

                                createMarker(obj.getJSONObject(i).getDouble("lat"), obj.getJSONObject(i).getDouble("lng"), obj.getJSONObject(i).getString("name"),
                                        "Available parking slots - " + obj.getJSONObject(i).getInt("availableSlots") + "/"+obj.getJSONObject(i).getInt("numOfSlots"),obj.getJSONObject(i).getString("prkType"),obj.getJSONObject(i).getInt("availableSlots"));

                            }else if(filtering.equals("outdoor") && obj.getJSONObject(i).getString("prkType").equals("Outdoor")){

                                createMarker(obj.getJSONObject(i).getDouble("lat"), obj.getJSONObject(i).getDouble("lng"), obj.getJSONObject(i).getString("name"),
                                        "Available parking slots - " + obj.getJSONObject(i).getInt("availableSlots") + "/"+obj.getJSONObject(i).getInt("numOfSlots"),obj.getJSONObject(i).getString("prkType"),obj.getJSONObject(i).getInt("availableSlots"));

                            }else if(filtering.equals("both")){

                                createMarker(obj.getJSONObject(i).getDouble("lat"), obj.getJSONObject(i).getDouble("lng"), obj.getJSONObject(i).getString("name"),
                                        "Available parking slots - " + obj.getJSONObject(i).getInt("availableSlots") + "/"+obj.getJSONObject(i).getInt("numOfSlots"),obj.getJSONObject(i).getString("prkType"),obj.getJSONObject(i).getInt("availableSlots"));

                            }

                        } catch (JSONException e) {
                            return;
                        }
                    }
                }
            });
        }

    };

    protected void createMarker(double latitude, double longitude, String title, String snippet, String prkType, int avaliable) {

        if(avaliable > 5){

            if(prkType.equals("Indoor")){
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .title(title)
                        .snippet(snippet)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.in5p)));
            }else{
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .title(title)
                        .snippet(snippet)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.out5m)));
            }

        }else if(avaliable<5 && avaliable>2){

            if(prkType.equals("Indoor")){
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .title(title)
                        .snippet(snippet)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.in2p)));
            }else{
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .title(title)
                        .snippet(snippet)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.out2p)));
            }

        }else{

            if(prkType.equals("Indoor")){
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .title(title)
                        .snippet(snippet)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.in2m)));
            }else{
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .anchor(0.5f, 0.5f)
                        .title(title)
                        .snippet(snippet)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.out2m)));
            }

        }



    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            System.out.println(" >>>>>>>>>>>> socket.io connceted --- ");
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
            i.putExtra("isSalatite", isSataliteMode);
            i.putExtra("filter", filtering);
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
        mMap.moveCamera(CameraUpdateFactory.zoomTo(14));

        if (isSataliteMode == true){
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }

        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location arg0) {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(arg0.getLatitude(), arg0.getLongitude())));
            }
        });
    }

}
