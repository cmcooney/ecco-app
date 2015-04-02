package com.eccoTCPAsync.app;

import android.app.Activity;
//import android.app.Fragment;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
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
public class ListResultFragment extends Fragment {
    String TAG = "ListResultFragment";
    //SearchAsyncResponse asyncResponse;
    PassQueryUri makeMyQueryUri;
    BuildFullTextFrag buildFullTextFragment;
    BuildTOCFrag buildTOCFragment;
    QuickLinkBibFragment quickLinkBibFragment;
    //Boolean report_search;
    ProgressDialog dialog;
    public Boolean report_search;
    public Boolean conc_report;
    public Boolean bibliography_report;
    public Boolean quick_link_search;
    public String spinner_value;
    public Context context;


    //public interface SearchAsyncResponse {
    //    public void asyncFinished(ArrayList all_results, int total_hits, int start_hit, Boolean bibliography_report);
   // }

    public interface PassQueryUri {
        public void makeMyQueryUri(String my_start_hit, String my_end_hit, String spinner_value);
    }

    public interface BuildFullTextFrag {
        public void buildFullTextFragment(String[] build_query_array, String[] offsets);
    }

    public interface BuildTOCFrag {
        public void buildTOCFragment(String[] pid_toc_query_array);
    }

    public interface QuickLinkBibFragment {
        public void quickLinkBibFragment(String ql_bib_url);
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        context = getActivity().getApplicationContext();
        Log.i(TAG, " onAttach works...");
        try {
            //asyncResponse = (SearchAsyncResponse) activity;
            makeMyQueryUri = (PassQueryUri) activity;
            buildFullTextFragment = (BuildFullTextFrag) activity;
            buildTOCFragment = (BuildTOCFrag) activity;
            quickLinkBibFragment = (QuickLinkBibFragment) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement interface correctly");
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        Log.i(TAG, "Made it to ListResultFragment!");
        View my_view = inflater.inflate(R.layout.list_result_linear, container, false);
        Log.i(TAG + " In onCreateView ", my_view.toString());

        return my_view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        Activity back_activity = this.getActivity();
        Log.i(TAG, " back_activity: " + back_activity);
        Bundle bundle = this.getArguments();
        Log.i(TAG + " In onActivityCreated; your bundle: ", bundle.toString());
        String query_uri = bundle.getString("query_uri");
        Log.i(TAG + " fulltext query string: ", query_uri);
        //Log.i(TAG + " Context being sent to GetResults: ", this.toString());
        //report_search = true;

        //GetResults gr = new GetResults(getActivity());
        //gr.execute(query_uri);
        new ListResults().execute(query_uri);

    }

    private class ListResults extends AsyncTask<String, Void, ArrayList> {

        public String field = "";
        public String text = "";
        public String citation = "";
        public String list_shrtcit = "";
        public String offsets = "";
        public String philoid = "";
        public String title = "";
        public String next = "";
        public String cite = "";
        public String author = "";
        public int total_hits = 0;
        public int start_hit = 0;
        int hit_number = 0;
        public float chuck_float =  Float.parseFloat(".25");


        public ListResults(){}
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

                // test for kind of search //
                if (search_URI.contains("report=")){
                    report_search = true;
                    spinner_value = "concordance";
                    if (search_URI.contains("dispatcher.py?report=concordance")){
                        Log.i(TAG, "We have concordance!");
                        conc_report = true;
                    }
                    else if (search_URI.contains("report=bibliography")){
                        Log.i(TAG, "We have bibliography search!");
                        bibliography_report = true;
                    }
                }

                else {
                    report_search = false;
                    if (search_URI.contains("landing_page_content.py")) {
                        Log.i(TAG, "We have quick link search");
                        quick_link_search = true;
                    }
                }

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
                        if (report_search) {
                            Log.i(TAG + "  Running a report search", "Boolean is true!");

                            if (conc_report != null){
                                Log.i(TAG, "  concordance report");
                                JSONArray jsonArray = new JSONArray(line.toString());
                                for (int i = 0; i< jsonArray.length(); i++){
                                    JSONObject result_line = jsonArray.getJSONObject(i);
                                    text = result_line.getString("text");
                                    philoid = result_line.getString("philo_id");
                                    offsets = result_line.getString("offsets");
                                    citation = result_line.getString("citation");
                                    list_shrtcit = result_line.getString("shrtcit"); // not using shrtcit in list; only fulltext
                                    total_hits = result_line.getInt("hit_count");
                                    start_hit = result_line.getInt("start");
                                    //Log.i(TAG, "Shrtcit and philoid == " + list_shrtcit + " " + philoid);
                                    hit_number = i + start_hit;
                                    String out_pair = "<off>" + offsets + "</off><pid>" + philoid + "</pid><hit>" + hit_number + "</hit>) " + citation + "<cmc>" + text;
                                    all_results.add(out_pair);
                                }
                            }

                            else if (bibliography_report != null){
                                Log.i(TAG, " bibliography report");
                                JSONArray jsonArray = new JSONArray(line.toString());
                                Log.i(TAG, " bib report length: " + jsonArray.length());
                                for (int i = 0; i< jsonArray.length(); i++){
                                    JSONObject result_line = jsonArray.getJSONObject(i);
                                    philoid = result_line.getString("philo_id");
                                    citation = result_line.getString("citation");
                                    text = result_line.getString("text");
                                    total_hits = result_line.getInt("hit_count");
                                    hit_number = i + 1;
                                    String out_pair = "<pid>" + philoid + "</pid><hit>" + hit_number + "</hit>) " + citation + "<cmc>" + text;
                                    all_results.add(out_pair);
                                }
                            }

                        }
                        else {
                            if (quick_link_search != null) {
                                Log.i(TAG, " quick link");
                                JSONArray jsonArray = new JSONArray(line.toString());
                                Log.i(TAG, " Your quick link json: " + jsonArray.length());
                                total_hits = jsonArray.length();
                                for (int i = 0; i< jsonArray.length(); i++){
                                    JSONObject result_line = jsonArray.getJSONObject(i);
                                    cite = result_line.getString("cite");
                                    //author = result_line.getString("author");
                                    hit_number = i + 1;
                                    cite = cite.replace("<a href", "<a link");
                                    String out_pair = "<hit>" + hit_number + "</hit>) " + cite + " title(s)";
                                    all_results.add(out_pair);
                                }
                            }
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

            Log.i(TAG, "  total hits passed! " + total_hits);
            Log.i(TAG, " Starting hit for this set of results == " + start_hit);
            //Log.i(TAG, " asyncFinished results: " + all_results);
            final Boolean bibliography_report2pass = bibliography_report; // need this for 'inner class'

            final TextView mTextView;
            final ListView mListView;
            final String count_display;

            if (getView() == null) {
                View view = LayoutInflater.from(context).inflate(R.layout.list_result_linear, null);
                Log.i(TAG, " View was null: " + view.toString());
                mTextView = (TextView) view.findViewById(R.id.hit_count);
                mListView = (ListView) view.findViewById(R.id.results_list);
                //count_display = context.getResources().getQuantityString(R.plurals.search_results_count, total_hits, total_hits);
            } else {
                Log.i(TAG, " The View from here: " + getView().toString());
                mTextView = (TextView) getView().findViewById(R.id.hit_count);
                mListView = (ListView) getView().findViewById(R.id.results_list);
                //count_display = getResources().getQuantityString(R.plurals.search_results_count, total_hits, total_hits);
            }

            count_display = context.getResources().getQuantityString(R.plurals.search_results_count, total_hits, total_hits);

            mTextView.setText(count_display);
            if (all_results != null && !all_results.isEmpty()) {

                //if (all_results.contains("QUICKLINK")){
                if (quick_link_search != null){
                    try {
                        Log.i(TAG, " Need QuickLinkAdapter");
                        DisplayQuicklinkAdapter linkAdapter = new DisplayQuicklinkAdapter(context, R.layout.result, all_results);
                        mListView.setAdapter(linkAdapter);
                    }
                    catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override

                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String one_more_click_link = mListView.getItemAtPosition(position).toString();
                            Log.i(TAG, " Uggggh: " + one_more_click_link);
                            Pattern pid_regex = Pattern.compile("<a link=\"([^>]*)\">");
                            Matcher pid_match = pid_regex.matcher(one_more_click_link);
                            if (pid_match.find()){
                                Log.i(TAG, " Build a link, please: " + pid_match.group(1));
                                String ql_bib_url = pid_match.group(1);
                                //ql_bib_url = ql_bib_url.replace("./?q=", "");
                                quickLinkBibFragment.quickLinkBibFragment(ql_bib_url);
                            }

                        }
                    });

                } // end quick link handling

                else { // now handle standard conc and bib results
                    try {
                        Log.i(TAG + "  Sending results to Results Adapter:", "verified");
                        DisplayResultsAdapter outAdapter = new DisplayResultsAdapter(context, R.layout.result, all_results);
                        mListView.setAdapter(outAdapter);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    if (total_hits > 25 && bibliography_report == null) {
                        int next_start_hit = start_hit + 25;
                        int next_end_hit = next_start_hit + 24;

                        final String my_start_hit = Integer.toString(next_start_hit);
                        final String my_end_hit = Integer.toString(next_end_hit);
                        final String my_prev_start_hit = Integer.toString(start_hit - 25);
                        final String my_prev_end_hit = Integer.toString(start_hit - 1);

                        //LayoutInflater inflater = (LayoutInflater) getSystemService(context.LAYOUT_INFLATER_SERVICE);
                        View buttons_view = LayoutInflater.from(context).inflate(R.layout.image_buttons, null);

                        ImageButton prev_btn = (ImageButton) buttons_view.findViewById(R.id.ll_previous);
                        ImageButton next_btn = (ImageButton) buttons_view.findViewById(R.id.ll_next);

                        next_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.i(TAG, "You clicked next!");
                                makeMyQueryUri.makeMyQueryUri(my_start_hit, my_end_hit, spinner_value);
                                //makeMyQueryUri(my_start_hit, my_end_hit);
                            }
                        });

                        prev_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.i(TAG, "You clicked previous!");
                                //makeMyQueryUri(my_prev_start_hit, my_prev_end_hit);
                                makeMyQueryUri.makeMyQueryUri(my_prev_start_hit, my_prev_end_hit, spinner_value);
                            }
                        });

                        if (start_hit == 1) {
                            //prev_btn.setVisibility(View.INVISIBLE);
                            prev_btn.setAlpha(chuck_float);
                            prev_btn.setOnClickListener(null);
                        }
                        else if (total_hits < next_start_hit) {
                            //next_btn.setVisibility(View.INVISIBLE);
                            next_btn.setAlpha(chuck_float);
                            next_btn.setOnClickListener(null);
                        }

                        final View insertPoint;
                        if (getView() == null){
                            View view = LayoutInflater.from(context).inflate(R.layout.list_result_linear, null);
                            insertPoint = view.findViewById(R.id.list_res_linear);
                            }
                        else {
                            insertPoint = getView().findViewById(R.id.list_res_linear);

                        }
                        ((ViewGroup) insertPoint).addView(buttons_view, 1, new
                                ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    } // end next + prev button code

                    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override

                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            String single_result_hit = mListView.getItemAtPosition(position).toString();
                            String pid_query_string_match = "";
                            String pid_address = "";
                            Pattern pid_regex = Pattern.compile("<pid>([^<]*)</pid>");
                            Matcher pid_match = pid_regex.matcher(single_result_hit);
                            String offsets = "";
                            Pattern offset_regex = Pattern.compile("<off>([^<]*)</off>");
                            Matcher off_match = offset_regex.matcher(single_result_hit);

                            if (bibliography_report2pass == null){
                                if (pid_match.find() && off_match.find()) {
                                    pid_address = pid_match.group(1);
                                    pid_address = pid_address.replaceAll("\\[", "").replaceAll("\\]", "");
                                    offsets = off_match.group(1);
                                    offsets = offsets.replaceAll("\\[", "").replaceAll("\\]", "");

                                    Log.i(TAG, " Your philoID && offsets: " + pid_address + "|" + offsets);

                                    String[] pid_address_array = pid_address.split(",");
                                    String[] offsets_2pass = offsets.split(",");
                                    buildFullTextFragment.buildFullTextFragment(pid_address_array, offsets_2pass);
                                }
                            }
                            else {
                                Log.i(TAG, " This is a bibliography report, need different stuff");
                                Log.i(TAG, " Goodies to get your TOC " + single_result_hit);
                                if (pid_match.find()){
                                    pid_query_string_match = pid_match.group(1);
                                    String[] pid_query_array = pid_query_string_match.split(",");
                                    buildTOCFragment.buildTOCFragment(pid_query_array);

                                }
                            }
                        }
                    }); // end click listener
                } // end of bib and conc result handling

            } // end of code handling queries with results

            else { // Generate no results message in button //
               Log.i(TAG, "NO RESULTS!");
               final Dialog dialog = new Dialog(context);
               dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
               dialog.setContentView(R.layout.no_results_dialog);
               dialog.getWindow().getAttributes().dimAmount = 0;
               dialog.setCanceledOnTouchOutside(true);
               dialog.show();
            }

        } // end onPostExecute

    } // end AsyncTask


} // LAST AND FINAL Bracket...
