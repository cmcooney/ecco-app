package com.eccoTCPAsync.app;

import android.app.Activity;
//import android.app.Fragment;
import android.app.ProgressDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by cmcooney on 6/6/14.
 */
public class FreqResultFragment extends Fragment {
    String TAG = "FreqResultFragment";
    BuildListFragment buildListFragment;
    ProgressDialog dialog;
    public String uri_authority = "condorcet.uchicago.edu";
    public String philo_dir = "philologic4";
    public String build_name = "ecco_tcp_demo";
    public WebView mWebView;
    public TextView mTextView;
    public String freq_search_term = "";
    public Context context;

    public interface BuildListFragment {
        public void buildListFragment(String list_query_uri);
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        context = getActivity().getApplicationContext();
        Log.i(TAG, " onAttach works...");
        try {
            buildListFragment = (BuildListFragment) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement AsyncResponse4");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        Log.i(TAG, "Made it to FreqResultFragment!");
        View view = inflater.inflate(R.layout.freq_result_linear, container, false);
        Log.i(TAG + " In onCreateView ", view.toString());
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = this.getArguments();
        Log.i(TAG + " In onActivityCreated; your bundle: ", bundle.toString());
        String query_uri = bundle.getString("query_uri");
        Log.i(TAG + " fulltext query string: ", query_uri);
        Log.i(TAG + " Context being sent to GetResults: ", this.toString());
        new FreqResults().execute(query_uri);
    }

    private class FreqResults extends AsyncTask<String, Void, ArrayList> {

        public String field = "";
        public String count = "";
        public String search_term = "";
        public String text = "";
        public String freq_citation = "";
        public String freq_url = "";
        public String freq_results = "";
        String[] metadata_values = {};
        public String title = "";
        public String next = "";
        int hit_number = 0;

        public FreqResults(){}
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
                        //Log.i(TAG + "  Your string: ", line.toString());
                        Log.i(TAG + "  Running a report search", "Boolean is true!");

                        Log.i(TAG, "  freq report");
                        JSONArray jsonArray = new JSONArray(line.toString());
                        for (int i = 0; i< jsonArray.length(); i++){
                            JSONObject result_line = jsonArray.getJSONObject(i);
                            hit_number = i + 1;
                            field = result_line.getString("frequency_field");
                            freq_citation = result_line.getString("bib_values");
                            count = result_line.getString("count");
                            int count_value = Integer.parseInt(count);
                            freq_url = result_line.getString("url");
                            search_term = result_line.getString("search_term");
                            freq_results = result_line.getString("results");
                            freq_results = freq_results.replaceAll("\n", "");
                            String out_instance = "";
                            //Log.i(TAG, " Metadata values:  " +  field + " " + search_term + " " + freq_citation);
                            metadata_values = new String[] {field, search_term, freq_citation};
                            if (count_value > 1) {
                                out_instance = "instances";
                            }
                            else {
                                out_instance = "instance";
                            }
                            String out_pair = "<div class=\"freq_result\">" + hit_number + ") " + freq_results +
                                    "<div class=\"freq_count\"><a href=\"" + freq_url + "\">" +
                                    count + " " + out_instance + "</a></div></div>";
                            all_results.add(out_pair);
                        }
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
        } // end doInBackGround

        @Override
        protected void onPostExecute(ArrayList all_results) {
            if (dialog != null){
                dialog.dismiss();
            }

            if (all_results != null && !all_results.isEmpty()) {
                //Log.i(TAG + " Your freq result: ", all_results.toString());
                Log.i(TAG, " Freq params to display: " + field + " " + metadata_values);
                String freq_search_value = metadata_values[0];
                freq_search_term = metadata_values[1];
                String extra_metadata = metadata_values[2];
                extra_metadata = extra_metadata.replaceAll("[{}\"]", "");
                //String freq_params_to_display = "<div class=\"title\">Frequency of \"" + search_term + "\" by " + field + "</div>";
                String freq_params_to_display = "<div class=\"title\">Frequency of \"" + freq_search_term + "\" by " + freq_search_value;

                if (!extra_metadata.isEmpty()) {
                    freq_params_to_display = freq_params_to_display + ", " + extra_metadata + "</div>";
                } else {
                    freq_params_to_display = freq_params_to_display + "</div>";
                }

                Log.i(TAG, " Display: " + freq_params_to_display);
                String html_header = "<html><head><link href=\"philoreader.css\" type=\"text/css\" rel=\"stylesheet\"></head>";
                String results_string = all_results.toString();
                results_string = results_string.replaceAll("[\\[\\]]", "");
                //results_string = results_string.replace("[\\]]", "");
                results_string = results_string.replaceAll("</div>,", "</div><br>");
                results_string = "<body>" + html_header + results_string + "</body>";
                //Log.i(TAG, " Results string: " + results_string);
                if (getView() == null){
                    View view = LayoutInflater.from(context).inflate(R.layout.freq_result_linear, null);
                    mTextView = (TextView) view.findViewById(R.id.freq_params);
                    mWebView = (WebView) view.findViewById(R.id.freq_results_list);
                }
                else {
                    mTextView = (TextView) getView().findViewById(R.id.freq_params);
                    mWebView = (WebView) getView().findViewById(R.id.freq_results_list);
                }

                Log.i(TAG, " Textview: " + mTextView.toString());
                mTextView.setText(Html.fromHtml(freq_params_to_display));

                mWebView.setWebViewClient(new WebViewClient() {

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        Log.i(TAG, " Your URL: " + url);
                        if (url != null && url.contains("dispatcher.py")) {
                            url = url.replace("file:///android_asset/", "");
                            url = url.replace("&report=concordance", "");
                            url = url.replace("dispatcher.py/?", "dispatcher.py?report=concordance&");
                            String my_query_uri = "http://" + uri_authority + "/" + philo_dir + "/"+ build_name + "/" + url +
                                    "&format=json";
                            Log.i(TAG, "  FREQ query_uri: " + my_query_uri);
                            buildListFragment.buildListFragment(my_query_uri);
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
                Log.i(TAG, " WebView: " + mWebView.toString());
                mWebView.loadDataWithBaseURL("file:///android_asset/", results_string, "text/html", "utf-8", "");

            }

            else {
                // Generate no results message in button //
                Log.i(TAG, "NO RESULTS!");
                final Dialog dialog = new Dialog(context);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.no_results_dialog);
                dialog.getWindow().getAttributes().dimAmount = 0;
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
            }

        } //end onPostExecute

    } // end async

}