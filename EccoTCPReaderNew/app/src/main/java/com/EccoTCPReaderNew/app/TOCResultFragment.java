package com.EccoTCPReaderNew.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;

//import android.app.Fragment;


/**
 * Created by cmcooney on 6/4/14.
 */
public class TOCResultFragment extends Fragment {
    String TAG = "TOCResultFragment";
    BuildFullTextFragmentNew buildFullTextFragmentNew;
    ProgressDialog dialog;
    WebView mWebView;
    public Context context;

    public interface BuildFullTextFragmentNew {
        public void buildFullTextFragmentNew(String url);
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        context = getActivity().getApplicationContext();
        Log.i(TAG, " onAttach works...");
        try {
            buildFullTextFragmentNew = (BuildFullTextFragmentNew) activity;
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

        public String text = "";
        public String title = "";
        public String author = "";
        public String date = "";
        public String pub_date = "";
        public String pub_place = "";
        public String publisher = "";
        public String next = "";
        public String toc_title = "";
        public String toc_string = "";
        public String out_pair = "";

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
                        //Log.i(TAG, " Building TOC array");
                        JSONObject jsonObject = new JSONObject(line);
                        JSONArray jsonArray = jsonObject.getJSONArray("toc");
                        JSONObject cit_jsonObject = jsonObject.getJSONObject("citation");

                        JSONObject metadata_fields_jsonObject = jsonObject.getJSONObject("metadata_fields");
                        title = metadata_fields_jsonObject.getString("title");
                        author = metadata_fields_jsonObject.getString("author");
                        date = metadata_fields_jsonObject.getString("date");
                        pub_date = metadata_fields_jsonObject.getString("pub_date");
                        pub_place = metadata_fields_jsonObject.getString("pub_place");
                        publisher = metadata_fields_jsonObject.getString("publisher");

                        String pub_info = "";

                        if (!pub_place.isEmpty()){
                            pub_info = pub_place;
                        }

                        if (!publisher.isEmpty()){
                            pub_info = pub_info + " " + publisher;
                        }

                        if (!pub_date.isEmpty()){
                            pub_info = pub_info + " " + pub_date;
                        }
                        else {
                            pub_info = pub_info + " " + date;
                        }

                        //citation = author + " <a data-id=\"" + philo_id + "\"><i>" + title + "</i> [<b>" + date + "</b>]</a>";

                        //JSONObject title_jsonObject = cit_jsonObject.getJSONObject("title");
                        //JSONObject author_jsonObject = cit_jsonObject.getJSONObject("author");
                        //title = title_jsonObject.getString("label");
                        //author = author_jsonObject.getString("label");
                        //JSONObject date_jsonObject = cit_jsonObject.getJSONObject("date");
                        //String date = date_jsonObject.getString("label");

                        toc_title = author + " <span class='biblio_cite'><i>" + title + "</i> " + pub_info + "</span>";
                        toc_string = "";
                        for (int i = 0; i< jsonArray.length(); i++) {
                            JSONObject toc_object = jsonArray.getJSONObject(i);
                            String section = toc_object.getString("label");
                            String philo_type = toc_object.getString("philo_type");
                            String philo_id = toc_object.getString("philo_id");
                            String section_href = "/reports/navigation.py?report=navigate&philo_id=" + philo_id;
                            toc_string += "<div class=\"toc-" + philo_type + "\"><a href=\"" + section_href + "\">" + section + "</a></div>";
                        }
                        Log.i(TAG, toc_string);
                        out_pair = "<title>" + toc_title + "</title>" + toc_string;
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
            String results_string = results_array.replaceAll("^\\[", "");
            results_string = results_string.replaceAll("\\]$", "");
            results_string = results_string.replace("<title>", "<div class=\"toc-title\">");
            results_string = results_string.replace("</title>", "</div>");
            results_string =  results_string.replace("|", "&nbsp;");

            String html_header = "<html><head><link href=\"philoreader.css\" type=\"text/css\" rel=\"stylesheet\"></head>";
            results_string = "<body><div class=\"toc-body\">" + html_header + results_string + "</div></body>";

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
                    if (url != null) {
                        buildFullTextFragmentNew.buildFullTextFragmentNew(url);
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

