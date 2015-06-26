package com.EccoTCPReaderNew.app;

import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by cmcooney on 8/28/14.
 */
public class MyWebViewClient extends WebViewClient {

    public boolean scaleChangedRunnablePending = false;
    private static final String TAG = "MyWebViewClient";

    @Override
    public void onScaleChanged(final WebView webView, float oldScale, float newScale) {
        //Log.i(TAG, " Futzing with Scale " + oldScale + " " + newScale);
        if (scaleChangedRunnablePending) return;
        webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Log.i(TAG , " Am I in the run?");
                //webView.loadUrl("javascript:document.getElementsByTagName('body')[0].style.width=window.innerWidth+'px';");
                //webView.loadUrl("javascript:document.getElementsByTagName('body')[0].style.width=window.innerWidth;");
                webView.loadUrl("javascript:document.getElementsByTagName('body')[0].style.marginRight=20px;");
                scaleChangedRunnablePending = false;
            }
        }, 1);
    }
}
