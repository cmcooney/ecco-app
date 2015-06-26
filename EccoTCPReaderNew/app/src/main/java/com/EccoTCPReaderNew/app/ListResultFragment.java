package com.EccoTCPReaderNew.app;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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

//import android.app.Fragment;


/**
 * Created by cmcooney on 6/6/14.
 */
public class ListResultFragment extends Fragment {
    String TAG = "ListResultFragment";
    //SearchAsyncResponse asyncResponse;
    PassQueryUri makeMyQueryUri;
    BuildFullTextFragmentNew buildFullTextFragmentNew;
    BuildTOCFrag buildTOCFragment;
    QuickLinkBibFragment quickLinkBibFragment;
    //Boolean report_search;
    ProgressDialog dialog;
    Dialog no_res_dialog;
    public Boolean report_search;
    public Boolean conc_report;
    public Boolean bibliography_report;
    public Boolean who;
    public Boolean quick_link_search;
    public Boolean quick_link_author;
    public Boolean quick_link_title;
    public String spinner_value;
    public Context context;


    //public interface SearchAsyncResponse {
    //    public void asyncFinished(ArrayList all_results, int total_hits, int start_hit, Boolean bibliography_report);
   // }

    public interface PassQueryUri {
        public void makeMyQueryUri(String my_start_hit, String my_end_hit, String spinner_value);
    }

    public interface BuildFullTextFragmentNew {
        public void buildFullTextFragmentNew(String url);
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
            buildFullTextFragmentNew = (BuildFullTextFragmentNew) activity;
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
        Bundle bundle = this.getArguments();
        String query_uri = bundle.getString("query_uri");
        //Log.i(TAG + " Context being sent to GetResults: ", this.toString());
        //report_search = true;

        //GetResults gr = new GetResults(getActivity());
        //gr.execute(query_uri);
        new ListResults().execute(query_uri);

    }

    private class ListResults extends AsyncTask<String, Void, ArrayList> {

        public String text = "";
        public String offsets = "";
        public String philoid = "";
        public String title = "";
        public String author = "";
        public String speaker = "";
        public String speaker_link = "";
        public String date = "";
        public String print_cit = "";
        public String pub_date = "";
        public String pub_info = "";
        public String pub_place = "";
        public String publisher = "";
        public String next = "";
        public String cite = "";
        public String quick_link_link = "";
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
                    if (search_URI.contains("report=concordance")){
                        Log.i(TAG, "We have concordance!");
                        conc_report = true;
                    }
                    else if (search_URI.contains("report=bibliography")){
                        Pattern who_p = Pattern.compile("who=([^&]+)&start");
                        Matcher who_match = who_p.matcher(search_URI);
                        if (who_match.find()) {
                            //Log.i (TAG, " WHOOO!" + who_match.group(0));
                            who = true;
                        }
                        Log.i(TAG, "We have bibliography search!");
                        bibliography_report = true;
                    }
                }

                else {
                    report_search = false;
                    if (search_URI.contains("landing_page_content.py")) {
                        Log.i(TAG, "We have quick link search");
                        quick_link_search = true;
                        if (search_URI.contains("landing_page_content_type=author")){
                            quick_link_author = true;
                        }
                        else if (search_URI.contains("landing_page_content_type=title")){
                            quick_link_title = true;
                        }
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
                        //Log.i(TAG + "  Your string: ", line);
                        if (report_search) {
                            Log.i(TAG, "Running a report search");

                            JSONObject jsonObject = new JSONObject(line);
                            JSONArray jsonArray = jsonObject.getJSONArray("results");
                            JSONObject query_jsonObject = jsonObject.getJSONObject("query");
                            start_hit = query_jsonObject.getInt("start");
                            total_hits = jsonObject.getInt("results_length");

                            if (conc_report != null){
                                Log.i(TAG, "  concordance report");
                                for (int i = 0; i< jsonArray.length(); i++){
                                    JSONObject result_line = jsonArray.getJSONObject(i);
                                    text = result_line.getString("context");
                                    philoid = result_line.getString("philo_id");
                                    offsets = result_line.getString("bytes");

                                    //JSONObject fulltext_link_jsonObject = result_line.getJSONObject("citation_links");
                                    //String fulltext_line = fulltext_link_jsonObject.getString("div2");
                                    //fulltext_line = fulltext_line.replace("http://artflsrv02.uchicago.edu/philologic4/ecco-tcp/", "");
                                    //fulltext_line = fulltext_line + "&format=json";
                                    JSONObject cit_jsonObject = result_line.getJSONObject("citation");
                                    JSONObject div1_cit_jsonObject = cit_jsonObject.getJSONObject("div1");
                                    JSONObject div2_cit_jsonObject = cit_jsonObject.getJSONObject("div2");
                                    JSONObject div3_cit_jsonObject = cit_jsonObject.getJSONObject("div3");
                                    String fulltext_line = "";
                                    String link_to_context = "";
                                    if (div3_cit_jsonObject.length() > 0){
                                        fulltext_line = div3_cit_jsonObject.getString("href");
                                        link_to_context = div3_cit_jsonObject.getString("label");
                                    }
                                    else if (div2_cit_jsonObject.length() > 0) {
                                        fulltext_line = div2_cit_jsonObject.getString("href");
                                        link_to_context = div2_cit_jsonObject.getString("label");
                                    }
                                    else if (div1_cit_jsonObject.length() > 0){
                                        fulltext_line = div1_cit_jsonObject.getString("href");
                                        link_to_context = div1_cit_jsonObject.getString("label");
                                    }
                                    fulltext_line = fulltext_line.replace("http://artflsrv02.uchicago.edu/philologic4/ecco-tcp/", "");
                                    fulltext_line = fulltext_line + "&format=json";
                                    if (link_to_context.isEmpty()){
                                        link_to_context = "[Context]";
                                    }

                                    JSONObject metadata_fields_jsonObject = result_line.getJSONObject("metadata_fields");
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

                                    print_cit = author + " <i>" + title + "</i> " + pub_info;
                                    hit_number = i + start_hit;
                                    String out_pair = "<div class=\"conc_bibinfo\">" + hit_number + ") " + print_cit +
                                            " <a href=\"conc_result=" + fulltext_line + "\">" + link_to_context +
                                            "</a></div><div class=\"conc_result\">" + text + "</div><hr>";
                                    all_results.add(out_pair);
                                }
                            }

                            else if (bibliography_report != null){
                                Log.i(TAG, " bibliography report");

                                if (who != null){
                                    Log.i(TAG, " Time to mess with who");
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject result_line = jsonArray.getJSONObject(i);
                                        philoid = result_line.getString("philo_id");

                                        JSONObject cit_jsonObject = result_line.getJSONObject("citation");
                                        JSONObject speaker_jsonObject = cit_jsonObject.getJSONObject("para");
                                        speaker = speaker_jsonObject.getString("label");
                                        speaker_link = speaker_jsonObject.getString("href");
                                        speaker_link = speaker_link.replace("http://artflsrv02.uchicago.edu/philologic4/ecco-tcp/navigate/", "");
                                        speaker_link = speaker_link.replaceAll("/", "%20");

                                        speaker_link = "/reports/navigation.py?report=navigate&philo_id=" + speaker_link;

                                        Log.i(TAG, speaker_link);
                                        JSONObject line_jsonObject = cit_jsonObject.getJSONObject("page");
                                        String line_no = line_jsonObject.getString("label");
                                        line_no = line_no.replace("page", "line");
                                        JSONObject scene_jsonObject = cit_jsonObject.getJSONObject("div2");
                                        String act_scene = scene_jsonObject.getString("label");

                                        JSONObject meta_jsonObject = result_line.getJSONObject("metadata_fields");
                                        title = meta_jsonObject.getString("title");
                                        date = meta_jsonObject.getString("date");


                                        print_cit = title + " [<b>" + date + "</b>]";
                                        hit_number = i + start_hit;
                                        String out_pair = "<pid>" + speaker_link + "</pid><hit>" + hit_number + "</hit>) " + print_cit + "<cmc>Speaker: " + speaker +
                                                " <i>" + act_scene + " " + line_no + "</i>";

                                        //Log.i(TAG, " out pair: " + out_pair);
                                        all_results.add(out_pair);
                                    }
                                }
                                else {
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject result_line = jsonArray.getJSONObject(i);
                                        philoid = result_line.getString("philo_id");

                                        /*
                                        JSONObject cit_jsonObject = result_line.getJSONObject("citation");
                                        JSONObject title_jsonObject = cit_jsonObject.getJSONObject("title");
                                        title = title_jsonObject.getString("label");
                                        JSONObject author_jsonObject = cit_jsonObject.getJSONObject("author");
                                        author = author_jsonObject.getString("label");
                                        JSONObject date_jsonObject = cit_jsonObject.getJSONObject("date");
                                        date = date_jsonObject.getString("label");*/

                                        JSONObject metadata_fields_jsonObject = result_line.getJSONObject("metadata_fields");
                                        title = metadata_fields_jsonObject.getString("title");
                                        author = metadata_fields_jsonObject.getString("author");
                                        date = metadata_fields_jsonObject.getString("date");
                                        pub_date = metadata_fields_jsonObject.getString("pub_date");
                                        pub_place = metadata_fields_jsonObject.getString("pub_place");
                                        publisher = metadata_fields_jsonObject.getString("publisher");

                                        pub_info = "";

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
                                        //print_cit = author + " " + title + " [<b>" + date + "</b>]";
                                        //print_cit = author + " <i>" + title + "</i> " + pub_info;
                                        hit_number = i + start_hit;
                                        String out_pair = "<div class=\"author_bibinfo\">" + hit_number + ") " + author +
                                                " <a href=\"author_bibinfo_link=" + philoid + "\"><i>" + title + "</i></a> " + pub_info + "</div>";
                                        //Log.i(TAG, " out pair: " + out_pair);
                                        all_results.add(out_pair);
                                    }
                                }
                            }

                        }
                        else {
                            if (quick_link_search != null) {
                                JSONArray jsonArray = new JSONArray(line);
                                total_hits = jsonArray.length();
                                String cite_author = "";
                                String cite_title = "";
                                Integer author_title_count;
                                String out_author_count = "";
                                for (int i = 0; i< jsonArray.length(); i++){
                                    JSONObject result_line = jsonArray.getJSONObject(i);
                                    cite_author = result_line.getString("author");

                                    if (quick_link_title != null) {
                                        JSONObject metadata_fields_jsonObject = result_line.getJSONObject("metadata_fields");
                                        cite_author = metadata_fields_jsonObject.getString("author");
                                        date = metadata_fields_jsonObject.getString("date");
                                        pub_date = metadata_fields_jsonObject.getString("pub_date");
                                        pub_place = metadata_fields_jsonObject.getString("pub_place");
                                        publisher = metadata_fields_jsonObject.getString("publisher");

                                        pub_info = "";

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
                                        cite_title = metadata_fields_jsonObject.getString("title");
                                        //cite_title = result_line.getString("title");
                                    }
                                    if (quick_link_author != null){
                                        author_title_count = result_line.getInt("count");
                                        String counts = "";
                                        if (author_title_count > 1){
                                            counts = "titles";
                                        }
                                        else {
                                            counts = "title";
                                        }
                                        out_author_count = author_title_count.toString() + " " + counts;
                                    }
                                    quick_link_link = result_line.getString("url");
                                    quick_link_link = quick_link_link.replace("navigate/", "");
                                    quick_link_link = quick_link_link.replace("/table-of-contents", "");
                                    quick_link_link = quick_link_link.replaceAll("\"", "");
                                    hit_number = i + 1;
                                    //Log.i(TAG, quick_link_link);
                                    cite = cite.replace("<a href", "<a link");
                                    //String out_pair = "<hit>" + hit_number + "</hit>) <a link=\"" +  quick_link_link + "\">" + cite;
                                    //Log.i(TAG, out_pair);
                                    String out_pair = "";
                                    if (quick_link_author != null){
                                        Log.i(TAG, " QUICK LINK AUTHOR!");
                                        out_pair = "<div class=\"quicklink_author\">" + hit_number + ") " + cite_author +
                                                " <a href=\"get_titles=" +  quick_link_link + "\">" + out_author_count + "</a></div>";
                                    }
                                    else {
                                        //out_pair = "<div class=\"quicklink_title\">" + hit_number +
                                        //        ") " + cite_author + " <a href=\"" + quick_link_link + "\"><i>" + cite_title + "</i></a></div>";
                                        out_pair = "<div class=\"quicklink_title\">" + hit_number +
                                                ") " + cite_author + " <a href=\"" + quick_link_link + "\">" + cite_title + "</a> " + pub_info + "</div>";
                                    }
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
            final WebView mWebView;
            final String count_display;

            if (getView() == null) {
                View view = LayoutInflater.from(context).inflate(R.layout.list_result_linear, null);
                Log.i(TAG, " View was null: " + view.toString());
                mTextView = (TextView) view.findViewById(R.id.hit_count);
                //mListView = (ListView) view.findViewById(R.id.results_list);
                mWebView = (WebView) view.findViewById(R.id.results_list);
                //count_display = context.getResources().getQuantityString(R.plurals.search_results_count, total_hits, total_hits);
            } else {
                Log.i(TAG, " The View from here: " + getView().toString());
                mTextView = (TextView) getView().findViewById(R.id.hit_count);
                //mListView = (ListView) getView().findViewById(R.id.results_list);
                mWebView = (WebView) getView().findViewById(R.id.results_list);
                //count_display = getResources().getQuantityString(R.plurals.search_results_count, total_hits, total_hits);
            }

            count_display = context.getResources().getQuantityString(R.plurals.search_results_count, total_hits, total_hits);

            mTextView.setText(count_display);
            if (all_results != null && !all_results.isEmpty()) {

                if (quick_link_search != null){
                    try {
                        Log.i(TAG, " Need QuickLinkAdapter");
                        //DisplayQuicklinkAdapter linkAdapter = new DisplayQuicklinkAdapter(context, R.layout.result, all_results);
                        //mListView.setAdapter(linkAdapter);
                        String html_header = "<html><head><link href=\"philoreader.css\" type=\"text/css\" rel=\"stylesheet\"></head>";
                        String results_string = all_results.toString();
                        results_string = results_string.replaceAll("^\\[", "");
                        results_string = results_string.replaceAll("<hr>,", "<hr>");
                        results_string = results_string.replaceAll("<hr>]", "");
                        results_string = results_string.replaceAll("</div>,", "</div>");
                        results_string = results_string.replaceAll("</div>]", "</div>");
                        results_string = "<body>" + html_header + results_string + "</body>";
                        mWebView.setBackgroundColor(0x00000000);
                        mWebView.setWebViewClient(new WebViewClient() {

                            @Override
                            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                Log.i(TAG, " Your URL: " + url);

                                if (url != null && url.contains("get_titles")) {
                                    Log.i(TAG, " Author Title LINK: " + url);
                                    url = url.replace("file:///android_asset/get_titles=", "");
                                    quickLinkBibFragment.quickLinkBibFragment(url);
                                    return true;
                                }
                                else if (url != null) {
                                    Log.i(TAG, " TOC LINK: " + url);
                                    url = url.replace("file:///android_asset/", "");
                                    String[] pid_query_array = url.split(",");
                                    buildTOCFragment.buildTOCFragment(pid_query_array);
                                    return true;
                                } else {
                                    return false;
                                }
                            }
                        });
                        Log.i(TAG, " WebView: " + mWebView.toString());
                        mWebView.loadDataWithBaseURL("file:///android_asset/", results_string, "text/html", "utf-8", "");
                    }
                    catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    /*mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override

                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String one_more_click_link = mListView.getItemAtPosition(position).toString();
                            Pattern pid_regex = Pattern.compile("navigate/([^/]*)/table-of-contents");
                            Matcher pid_match = pid_regex.matcher(one_more_click_link);
                            if (pid_match.find()){
                                Log.i(TAG, " Build a link, please: " + pid_match.group(1));
                                String ql_bib_url = pid_match.group(1);
                                quickLinkBibFragment.quickLinkBibFragment(ql_bib_url);
                            }

                        }
                    });*/

                } // end quick link handling

                else { // now handle standard conc and bib results
                    try {
                        //DisplayResultsAdapter outAdapter = new DisplayResultsAdapter(context, R.layout.result, all_results);
                        //mListView.setAdapter(outAdapter);
                        mWebView.setBackgroundColor(0x00000000);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    //if (total_hits > 25 && bibliography_report == null) {
                    if (total_hits > 25 && bibliography_report != null || conc_report != null) {
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
                        if (total_hits < next_start_hit) {
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

                    String html_header = "<html><head><link href=\"philoreader.css\" type=\"text/css\" rel=\"stylesheet\"></head>";
                    String results_string = all_results.toString();
                    //results_string = results_string.replaceAll("[\\[\\]]", "");
                    results_string = results_string.replaceAll("^\\[", "");
                    results_string = results_string.replaceAll("<hr>,", "<hr>");
                    results_string = results_string.replaceAll("<hr>]", "");
                    results_string = results_string.replaceAll("</div>,", "</div>");
                    results_string = results_string.replaceAll("</div>]", "</div>");
                    results_string = "<body>" + html_header + results_string + "</body>";

                    //Log.i(TAG, results_string);

                    mWebView.setBackgroundColor(0x00000000);
                    mWebView.setWebViewClient(new WebViewClient() {

                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                            Log.i(TAG, " Your URL: " + url);
                            if (url != null && url.contains("toclink=")) {
                                Log.i(TAG, " TOC LINK: " + url);
                                url = url.replace("file:///android_asset/toclink=", "");
                                String[] pid_query_array = url.split(",");
                                buildTOCFragment.buildTOCFragment(pid_query_array);
                                return true;
                            } else if (url != null && url.contains("author_bibinfo_link")) {
                                url = url.replace("file:///android_asset/author_bibinfo_link=", "");
                                String[] pid_query_array = url.split(",");
                                buildTOCFragment.buildTOCFragment(pid_query_array);
                                return true;

                            } else if (url != null && url.contains("conc_result")) {
                                url = url.replace("file:///android_asset/conc_result=", "");
                                Log.i(TAG, "  FREQ query_uri: " + url);
                                buildFullTextFragmentNew.buildFullTextFragmentNew(url);
                                return true;
                            } else {
                                return false;
                            }
                        }
                    });

                    Log.i(TAG, " WebView: " + mWebView.toString());
                    mWebView.loadDataWithBaseURL("file:///android_asset/", results_string, "text/html", "utf-8", "");

                    /*
                    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override

                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            Log.i(TAG, "Click it!");
                            String single_result_hit = mListView.getItemAtPosition(position).toString();
                            String pid_query_string_match = "";
                            Pattern pid_regex = Pattern.compile("<pid>([^<]*)</pid>");
                            Matcher pid_match = pid_regex.matcher(single_result_hit);

                            if (bibliography_report2pass == null){
                                if (pid_match.find()) {
                                    String url = pid_match.group(1);
                                    Log.i(TAG, " The URL: " + url);
                                    buildFullTextFragmentNew.buildFullTextFragmentNew(url);
                                }
                            }
                            else {
                                Log.i(TAG, " This is a bibliography report, need different stuff");
                                Log.i(TAG, " Goodies to get your TOC " + single_result_hit);

                                if (pid_match.find()){
                                    if (who != null){
                                        String url = pid_match.group(1);
                                        Log.i(TAG, " Get who chunk");
                                        buildFullTextFragmentNew.buildFullTextFragmentNew(url);
                                    }
                                    else {
                                        pid_query_string_match = pid_match.group(1);
                                        String[] pid_query_array = pid_query_string_match.split(",");
                                        buildTOCFragment.buildTOCFragment(pid_query_array);
                                    }

                                }
                            }
                        }
                    }); // end click listener
                    */
                } // end of bib and conc result handling

            } // end of code handling queries with results

            else { // Generate no results message in button //
               Log.i(TAG, "NO RESULTS!");
               //final Dialog no_res_dialog = new Dialog(context);
               final Dialog dialog = new Dialog(getActivity());
               dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
               dialog.setContentView(R.layout.no_results_dialog);
               dialog.getWindow().getAttributes().dimAmount = 0;
               dialog.setCanceledOnTouchOutside(true);
               dialog.show();
            }

        } // end onPostExecute

    } // end AsyncTask


} // LAST AND FINAL Bracket...
