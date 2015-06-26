package com.EccoTCPReaderNew.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class Splash extends Activity {
    private static final String TAG = "SPLASH!";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, " You are in the splash activity!");

        setContentView(R.layout.splash);

        /*
        Button begin_button = (Button) findViewById(R.id.begin);

        begin_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, " Ya, splash screens blow...");
                launchMainActivity();
            }
        });*/

        WebView splashscreen = (WebView)findViewById(R.id.home_bg);

        splashscreen.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.i(TAG, " Touch me, baby: " + url);
                if (url != null && url.contains("touch_me")) {
                    launchMainActivity();
                }
                else {
                    view.getContext().startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                }
                return true;
            }

        });
        splashscreen.loadUrl("file:///android_asset/splash.html");

        /*
        int secondsDelayed = 40;
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Log.i(TAG, " postDelayed!");
                launchMainActivity();
            }
        }, secondsDelayed * 1000); */

    }

    private void launchMainActivity() {
        Intent mainIntent = new Intent(Splash.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
