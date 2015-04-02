package com.eccoTCPAsync.app;

/**
 * Created by cmcooney on 7/24/14.
 */

import android.app.Activity;
//import android.app.Fragment;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class InfoFragment extends Fragment {
    String TAG = "InfoFragment";

    QuickLinkSearch quickLinkSearch;

    public interface QuickLinkSearch {
        public void quickLinkSearch(String quick_link_url);
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        Log.i(TAG, " onAttach works...");
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
        Log.i(TAG, "Looking at info page in InfoFragment");
        View view = inflater.inflate(R.layout.info_page, container, false);
        Log.i(TAG + " In onCreateView ", view.toString());
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = this.getArguments();
        String file_name = bundle.getString("file_name");
        loadHtmlFile(file_name);
        Log.i(TAG + " In onActivityCreated; your bundle: ", bundle.toString());

    }

    public void loadHtmlFile (String file_name) {
        Log.i(TAG + " Loading HTML file: ", file_name.toString());


        final WebView wv = (WebView) getView().findViewById(R.id.info_wv);

        //Log.i(TAG, "Web view? " + wv.toString());
        wv.setWebViewClient(new WebViewClient() {

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

        wv.loadUrl("file:///android_asset/" + file_name);

    }

}
