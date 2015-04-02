package com.eccoTCPAsync.app;

import android.app.Activity;
//import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;


/**
 * Created by cmcooney on 6/4/14.
 */
public class TOCResultFragment extends Fragment {
    String TAG = "TOCResultFragment";
    BuildFullTextFrag buildFullTextFragment;
    ProgressDialog dialog;
    WebView mWebView;
    public Context context;

    public interface BuildFullTextFrag {
        public void buildFullTextFragment(String[] build_query_array, String[] offsets);
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        context = getActivity().getApplicationContext();
        Log.i(TAG, " onAttach works...");
        try {
            buildFullTextFragment = (BuildFullTextFrag) activity;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " Problem with asyncResponse");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        Log.i(TAG, "Made it to TOCResultFragment!");
        View view = inflater.inflate(R.layout.toc_result_linear, container, false);
        Log.i(TAG + " In onCreateView ", view.toString());
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = this.getArguments();
        Log.i(TAG + " In onActivityCreated; your bundle: ", bundle.toString());
        String new_query_uri = bundle.getString("query_uri");
        Log.i(TAG + " toc query string: ", new_query_uri);
        new TOCResults().execute(new_query_uri);

    }

    public class TOCResults extends AsyncTask<String, Void, ArrayList>{

        public String field = "";
        public String text = "";
        public String title = "";
        public String next = "";

        public TOCResults(){}

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (dialog == null){
                dialog = new ProgressDialog(getActivity());
                dialog.setMessage("Retrieving results.");
                dialog.show();
            }
        } // end onPreExecute

        @Override
        protected ArrayList doInBackground(String... urls) {
            BufferedReader reader = null;
            ArrayList<String> all_results = new ArrayList<String>();

            try {
                String search_URI = urls[0];
                Log.i(TAG + "  Search URI: ", search_URI);
                URI search_query = new URI(urls[0]);
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(search_query);

                HttpResponse response = httpclient.execute(httpget);
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();

                // read results into buffer //
                try {
                    reader = new BufferedReader(new InputStreamReader(content));
                    String line = "";

                    while ((line = reader.readLine()) != null) {
                        Log.i(TAG, " Building TOC array");
                        JSONObject jsonObject = new JSONObject(line.toString());
                        String toc_string = jsonObject.getString("toc");
                        String toc_title = jsonObject.getString("citation");
                        //String tmp_title = "Chuckler's Honor";
                        String out_pair = "<title>" + toc_title + "</title>" + toc_string;
                        all_results.add(out_pair);
                    }
                }
                catch (IOException exception) {
                    Log.e(TAG, "Here? IOException --> " + exception.toString());
                }
                // pro-forma cleanup //
                finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        }
                        catch (IOException exception) {
                            Log.e(TAG, "IOException --> " + exception.toString());
                        }
                    }
                }
            }
            // Exception for problems with HTTP connection //
            catch (Exception exception) {
                Log.e(TAG, "Trouble connecting -->" + exception.toString());
                return null;
            }
            //Log.i(TAG + "  Results string: ", all_results.toString());
            return all_results;
        }

        @Override
        protected void onPostExecute(ArrayList all_results) {
            if (dialog != null){
                dialog.dismiss();
            }

            Log.i(TAG + "  asyncFinished3", "Getting Results from onPostExecute!");
            //Log.i(TAG + " Your toc: ", all_results.toString());
            String results_array = all_results.toString();
            String results_string = results_array.replaceAll("[\\[\\]]", "");
            results_string = results_string.replace("<title>", "<div class=\"toc-title\">");
            results_string = results_string.replace("</title>", "</div>");
            results_string =  results_string.replace("|", "&nbsp;");

            //Log.i(TAG + " Grab id from here: ", results_string);

            String html_header = "<html><head><link href=\"philoreader.css\" type=\"text/css\" rel=\"stylesheet\"></head>";
            results_string = "<body>" + html_header + results_string + "</body>";

            if (getView() == null){
                View view = LayoutInflater.from(context).inflate(R.layout.toc_result_linear, null);
                mWebView = (WebView) view.findViewById(R.id.toc_wv_result);
                }
            else {
                mWebView = (WebView) getView().findViewById(R.id.toc_wv_result);
            }

            mWebView.getSettings().setBuiltInZoomControls(true);
            mWebView.setWebViewClient(new WebViewClient() {

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Log.i(TAG, " Your URL: " + url);
                    if (url != null && url.contains("dispatcher.py")) {
                        String[] chuck_uri_array = url.split("/");
                        String[] offsets = {};
                        String [] query_array = new String[9];
                        query_array[0] = chuck_uri_array[6];
                        query_array[1] = chuck_uri_array[7];
                        query_array[2] = chuck_uri_array[8];
                        query_array[3] = chuck_uri_array[9];
                        query_array[4] = "0";
                        query_array[5] = "0";
                        query_array[6] = "0";
                        query_array[7] = "0";
                        query_array[8] = "";
                        String chuck_uri = chuck_uri_array[6] + "/" + chuck_uri_array[7] + "/" + chuck_uri_array[8] + "/" + chuck_uri_array[9]
                                + "/" + chuck_uri_array[10] + "/" + chuck_uri_array[11];
                        Log.i(TAG, "F-ing with uri. Need: " + chuck_uri);
                        buildFullTextFragment.buildFullTextFragment(query_array, offsets);
                        return true;
                    } else {
                        return false;
                    }
                }
            });

            mWebView.loadDataWithBaseURL("file:///android_asset/", results_string, "text/html", "utf-8", "");
        } // end onPostExecute

    } // end asyncTask
} // end end

