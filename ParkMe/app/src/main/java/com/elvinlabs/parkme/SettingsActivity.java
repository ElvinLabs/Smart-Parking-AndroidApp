package com.elvinlabs.parkme;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class SettingsActivity extends ActionBarActivity {


    private boolean isSataliteMode = false;
    private String filter = "both";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Bundle extras = getIntent().getExtras();
        try{
            isSataliteMode = extras.getBoolean("isSalatite");
            filter = extras.getString("filter");
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+ extras.getString("filter"));
        }catch(NullPointerException e){
            System.out.println(e);
        }

        RadioButton salatite = (RadioButton) findViewById(R.id.satalite);
        RadioButton street = (RadioButton) findViewById(R.id.street);
        RadioButton indoor = (RadioButton) findViewById(R.id.indoor);
        RadioButton outdoor = (RadioButton) findViewById(R.id.outdoor);
        RadioButton both = (RadioButton) findViewById(R.id.both);

        if(isSataliteMode){
            salatite.setChecked(true);
            street.setChecked(false);
        }else{
            salatite.setChecked(false);
            street.setChecked(true);
        }

        if(filter.equals("indoor")){
            indoor.setChecked(true);
            outdoor.setChecked(false);
            both.setChecked(false);
        }else if(filter.equals("outdoor")){
            indoor.setChecked(false);
            outdoor.setChecked(true);
            both.setChecked(false);
        }else{
            indoor.setChecked(false);
            outdoor.setChecked(false);
            both.setChecked(true);
        }

        Button clickButton = (Button) findViewById(R.id.back);
        clickButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SettingsActivity.this, MapActivity.class);
                i.putExtra("isSalatite", isSataliteMode);
                i.putExtra("filter", filter);
                startActivity(i);
                finish();
            }
        });

    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        TextView txtv = (TextView)findViewById(R.id.textView9);

        switch(view.getId()) {
            case R.id.street:
                if (checked)
                    isSataliteMode = false;
                break;
            case R.id.satalite:
                if (checked)
                    isSataliteMode = true;
                break;
            case R.id.indoor:
                if (checked)
                    filter = "indoor";
                break;
            case R.id.outdoor:
                if (checked)
                    filter = "outdoor";
                break;
            case R.id.both:
                if (checked)
                    filter = "both";
                break;
        }
    }


}
