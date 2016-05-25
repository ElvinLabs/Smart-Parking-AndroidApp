package com.elvinlabs.parkme;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
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
//    private JSONArray obj = new JSONArray();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

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

        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on("new-client", onNewMessage);
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

    private Emitter.Listener onNewMessage = new Emitter.Listener() {

        @Override
        public void call(final Object... args) {


//            try{
            final JSONArray obj = (JSONArray)args[0];
//            }catch(Exception e){
//                System.out.println(e);
//            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toast.cancel();
                    for(int i=0; i<obj.length(); i++){
                        try{

                            System.out.println(" >>>>>>>>>>>> socket.io connceted --- "+ obj.getJSONObject(i).toString());

                            if(obj.getJSONObject(i).getInt("available")==3) {
                                mMap.addMarker(new MarkerOptions()
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.three))
                                        .anchor(0.0f, 0.0f)
                                        .title(obj.getJSONObject(i).getString("name"))
                                        .snippet("Available parking slots - " + obj.getJSONObject(i).getInt("available"))
                                        .position(new LatLng(obj.getJSONObject(i).getDouble("lat"), obj.getJSONObject(i).getDouble("lng"))));
                            }else if(obj.getJSONObject(i).getInt("available")==2){
                                mMap.addMarker(new MarkerOptions()
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.two))
                                        .anchor(0.0f, 0.0f)
                                        .title(obj.getJSONObject(i).getString("name"))
                                        .snippet("Available parking slots - " + obj.getJSONObject(i).getInt("available"))
                                        .position(new LatLng(obj.getJSONObject(i).getDouble("lat"), obj.getJSONObject(i).getDouble("lng"))));
                            }

                        } catch (JSONException e) {
                            return;
                        }
                    }
                }
            });
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
        mMap.moveCamera(CameraUpdateFactory.zoomTo(14));

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
