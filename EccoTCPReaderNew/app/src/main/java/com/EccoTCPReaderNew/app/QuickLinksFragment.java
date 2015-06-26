package com.EccoTCPReaderNew.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by cmcooney on 10/27/14.
 */
public class QuickLinksFragment extends Fragment {

    String TAG = "QuickLinksFragment";
    QuickLinkSearch quickLinkSearch;

    public interface QuickLinkSearch {
        public void quickLinkSearch(String quick_link_url);
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);

        try {
            quickLinkSearch = (QuickLinkSearch) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement interface correctly");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        Log.i(TAG, " This is the quicklinks view");
        //View view = inflater.inflate(R.layout.full_result_frag, container, false);
        View view = inflater.inflate(R.layout.quick_links_frame, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        WebView quickLinkWebView = (WebView) getView().findViewById(R.id.home_page);
        quickLinkWebView.getSettings().setJavaScriptEnabled(true);
        quickLinkWebView.getSettings().setBuiltInZoomControls(true);

        quickLinkWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.i(TAG, " Your URL: " + url);
                if (url != null && url.startsWith("http")) {
                    view.getContext().startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                }
                else if (url != null && url.contains("landing_page_content.py")) {
                    Log.i(TAG, " Have a Quick Link! " + url);
                    quickLinkSearch.quickLinkSearch(url);
                    return true;
                }
                else {
                    return false;
                }
            }
        });

        quickLinkWebView.loadUrl("file:///android_asset/quick_links.html");

    }
}
