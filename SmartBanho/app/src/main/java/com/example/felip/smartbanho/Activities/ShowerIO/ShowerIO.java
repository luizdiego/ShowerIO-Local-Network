package com.example.felip.smartbanho.Activities.ShowerIO;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.example.felip.smartbanho.Activities.Search.SearchForDevices;
import com.example.felip.smartbanho.R;
import com.example.felip.smartbanho.model.ShowerDevice;
import com.google.gson.Gson;


public class ShowerIO extends AppCompatActivity {

    private ProgressBar superProgressBar;
    private ImageView superImageView;
    private WebView superWebview;
    private LinearLayout superLinerLayout;
    private SharedPreferences sharedPreferences;
    public static final String ESP8266 = "esp8266";
    private String showerIOIpAddres = "http://";
    private ShowerDevice device;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_shower);

        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        superProgressBar = findViewById(R.id.myProgressBar);
        superWebview = findViewById(R.id.myWebView);
        superLinerLayout = findViewById(R.id.myLinearLayout);

        String selectedShower = getIntent().getExtras().getString("device");
        device = new Gson().fromJson(selectedShower, ShowerDevice.class);

        showerIOIpAddres = showerIOIpAddres + device.getIp() + "/index.html";


        superWebview.loadUrl(showerIOIpAddres);
        Bitmap bitmapShower = BitmapFactory.decodeResource(getResources(), R.drawable.ic_shower);

        superProgressBar.setMax(100);
        superWebview.getSettings().setJavaScriptEnabled(true);
        superWebview.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                onNotFoundEsp();
                super.onReceivedError(view, request, error);
            }


            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                superLinerLayout.setVisibility(View.VISIBLE);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                superLinerLayout.setVisibility(View.GONE);
                super.onPageFinished(view, url);

            }

        });
        superWebview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                superProgressBar.setProgress(newProgress);
            }


        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.super_menu, menu);
        return super.onCreateOptionsMenu(menu);


    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_back:
                onBackPressed();
                break;
            case R.id.menu_forward:
                onForwardPressed();
                break;
            case R.id.menu_refresh:
                superWebview.reload();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onForwardPressed() {
        if (superWebview.canGoForward()) {
            superWebview.goForward();
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        String selectedDevice = new Gson().toJson(device);
        Intent showerDetailActivity = new Intent(ShowerIO.this, ShowerDetailActivity.class);
        showerDetailActivity.putExtra("device", selectedDevice);
        startActivity(showerDetailActivity);
        finish();
    }

    public void onNotFoundEsp() {
        Intent searchForDevices = new Intent(this, SearchForDevices.class);
        startActivity(searchForDevices);
        finish();
    }
}
