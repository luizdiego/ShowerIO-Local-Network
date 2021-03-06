package com.example.felip.smartbanho.Activities.Search;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.example.felip.smartbanho.Activities.Error.DisplayMessageActivity;
import com.example.felip.smartbanho.Activities.ShowerIO.ShowerListActivity;
import com.example.felip.smartbanho.Process.Search.FullScan;
import com.example.felip.smartbanho.Process.Search.PartialScan;
import com.example.felip.smartbanho.Process.Search.SeekDevices;
import com.example.felip.smartbanho.Process.subnet.ScanIpAddressImpl;
import com.example.felip.smartbanho.R;
import com.example.felip.smartbanho.Rest.DeviceService;
import com.example.felip.smartbanho.Utils.SeekDevicesCallback;
import com.example.felip.smartbanho.model.ShowerDevice;
import com.github.ybq.android.spinkit.style.WanderingCubes;
import com.google.gson.Gson;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class SearchForDevices extends AppCompatActivity {

    private SeekDevices seekDevices;
    public ScanIpAddressImpl scanIpAddress;
    private List<String> ipList;
    private String espIpAddress;
    private SharedPreferences sharedPreferences;
    public static int RETRY = 0;
    private static int SPLASH_TIME_OUT = 4000;
    private Gson gson;
    private final String SHOWERIO = "ShowerIO";
    public List<ShowerDevice> showers;
    public RequestQueue requestQueue;
    private String scanType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.ipList = new ArrayList<>();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_search_for_devices);

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.spin_kit);
        WanderingCubes wanderingCubes = new WanderingCubes();
        progressBar.setIndeterminateDrawable(wanderingCubes);

        Log.d("searchForDevices Class", "Getting the ip from esp saved in the last session");
        ScanIpAddressImpl scanIpAddress = new ScanIpAddressImpl(this);
        this.scanIpAddress = scanIpAddress;

        this.scanType = getIntent().getExtras().getString("scanType");

        this.scanIpAddress.setSubnet();

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        showers = new ArrayList<ShowerDevice>();
        decodeScan();

        seekDevices.execute();

    }

    public void decodeScan() {
        String showersArrayAsString;
        sharedPreferences = getSharedPreferences(SHOWERIO, MODE_PRIVATE);
        showersArrayAsString = sharedPreferences.getString("listOfDevices", null);
        if (showersArrayAsString != null) {
            showers = Arrays.asList(new Gson().fromJson(showersArrayAsString, ShowerDevice[].class));
        }
        if (this.scanType.equals("FULLSCAN")) {
            this.seekDevices = new FullScan(this.scanIpAddress.subnet, this.showers, this.requestQueue, new SeekDevicesCallback() {
                @Override
                public void onServerCallback(Boolean status, List<ShowerDevice> foundDevices) {
                    showers = foundDevices;
                    onFinishedScan();
                }
            });
        } else if (this.scanType.equals("PARTIAL")) {
            this.seekDevices = new PartialScan(this.scanIpAddress.subnet, this.showers, this.requestQueue, new SeekDevicesCallback() {
                @Override
                public void onServerCallback(Boolean status, List<ShowerDevice> foundDevices) {
                    showers = foundDevices;
                    onFinishedScan();
                }
            });

        } else {
            //TODO - Handler to the error activity
        }

    }

    public void onFinishedScan() {

        if (showers.size() == 0) {
            Intent displayMessage = new Intent(SearchForDevices.this, DisplayMessageActivity.class);
            startActivity(displayMessage);
            finish();
        } else {
            //Saving on shared preferences to further authentication
            SharedPreferences.Editor editor = getSharedPreferences(SHOWERIO, MODE_PRIVATE).edit();

            Intent showerListActivity = new Intent(SearchForDevices.this, ShowerListActivity.class);
            //Serializing the object to json, to pass between the activities
            String showerArrayAsString = new Gson().toJson(showers);
            editor.putString("listOfDevices", showerArrayAsString);
            editor.commit();
            showerListActivity.putExtra("showerDevices", showerArrayAsString);
            startActivity(showerListActivity);
            finish();

        }
    }
}
