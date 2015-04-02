package com.eccoTCPAsync.app;

import android.app.Activity;
//import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by cmcooney on 6/4/14.
 */
public class FullResultFragment extends Fragment {
    String TAG = "FullResultFragment";
    BuildFullTextFrag buildFullTextFragment;
    BuildTOCFrag buildTOCFragment;
    PassBookmarkGoodies passBookmarkGoodies;
    ProgressDialog dialog;
    public WebView mWebView;
    public TextView mTextView;
    public String bookmarkPhiloId2Send = "";
    Context context;

    public interface BuildFullTextFrag {
        public void buildFullTextFragment(String[] build_query_array, String[] offsets);
    }

    public interface BuildTOCFrag {
        public void buildTOCFragment(String[] pid_toc_query_array);
    }

    public interface PassBookmarkGoodies {
        public void passBookmarkGoodies(String full_shrtcit, boolean addBookmarkBoolean, String bookmarkPhiloId2Send);
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        context = getActivity().getApplicationContext();
        Log.i(TAG, " onAttach works...");
        try {
            buildFullTextFragment = (BuildFullTextFrag) activity;
            buildTOCFragment = (BuildTOCFrag) activity;
            passBookmarkGoodies = (PassBookmarkGoodies) activity;
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
        Log.i(TAG, "Made it to FullResultFragment!");
        //View view = inflater.inflate(R.layout.full_result_frag, container, false);
        View view = inflater.inflate(R.layout.full_result_linear, container, false);
        Log.i(TAG + " In onCreateView ", view.toString());
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = this.getArguments();
        Log.i(TAG + " In onActivityCreated; your bundle: ", bundle.toString());
        String new_query_uri = bundle.getString("query_uri");
        Log.i(TAG + " fulltext query string: ", new_query_uri);
        Log.i(TAG + " Context being sent to GetResults: ", this.toString());
        String test_activity = this.getActivity().toString();
        Log.i(TAG + " What's the activity?: ", test_activity);
        //GetResults gr = new GetResults(getActivity());
        //gr.execute(new_query_uri);
        new FullResults().execute(new_query_uri);

    }

    private class FullResults extends AsyncTask<String, Void, ArrayList>{

        public String field = "";
        public String text = "";
        public String citation = "";
        public String full_shrtcit = "";
        public String full_philoid = "";
        public String title = "";
        public String prev = "";
        public String next = "";

        public float chuck_float =  Float.parseFloat(".25");

        public FullResults(){}
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
                        Log.i(TAG + "  Doing a fulltext search", "Boolean is false");
                        JSONObject jsonObject = new JSONObject(line.toString());
                        //String dummy_tag = "<cmc>" + "Read text!" + "</cmc>";
                        String json_string = jsonObject.getString("text");
                        full_shrtcit = jsonObject.getString("shrtcit");
                        full_philoid = jsonObject.getString("current");
                        prev = jsonObject.getString("prev");
                        next = jsonObject.getString("next");
                        citation = jsonObject.getString("citation");
                        String info_tag = "<shrt>" + full_shrtcit + "</shrt><title>" + citation + "</title>";
                        String out_pair = info_tag + json_string.toString();
                        all_results.add(out_pair);
                        Log.i(TAG, "  Creating FullText array! With shrtcit: " + full_shrtcit + " And fullcit: " + citation);

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

            Log.i(TAG + "  asyncFinished2", "Getting Results from onPostExecute!");
            Log.i(TAG + " Your current & prev & next philoids + shrtcit: ", full_philoid + " " + prev + "/" + next + "; " + full_shrtcit);
            String results_array = all_results.toString();

            full_philoid = full_philoid.replaceAll("[\\[\\]]", "");
            String[] bookMarkEdit = full_philoid.split(",");
            bookmarkPhiloId2Send = bookMarkEdit[0] + "/" + bookMarkEdit[1] + "/" + bookMarkEdit[2];
            //Log.i(TAG + " Fulltext results look like this: ", results_array);
            //String[] split_results_array = results_array.split("</shrt>");
            String[] split_results_array = results_array.split("</title>");
            String[] title_chunk = split_results_array[0].split("</shrt>");
            String title_string = title_chunk[1];
            title_string = title_string.replace("<title>", "<div class=\"title\">");
            title_string = title_string.replace("</title>", "</div>");
            title_string = title_string.replace("|", "&nbsp;");
            title_string = title_string.trim();
            Log.i(TAG, " Title string: " + title_string);
            String results_string = split_results_array[1];
            results_string = results_string.replaceFirst("]$","");
            //results_string = results_string.replace("<title>", "<div class=\"title\">");
            //results_string = results_string.replace("</title>", "</div>");
            //results_string =  results_string.replace("|", "&nbsp;");

            final String[] prev_array = prev.split(" ");
            final String[] next_array = next.split(" ");
            final String[] offsets = {};


            ImageButton next_btn;
            ImageButton prev_btn;

            if (getView() == null){
                View view = LayoutInflater.from(context).inflate(R.layout.full_result_linear, null);
                prev_btn = (ImageButton) view.findViewById(R.id.ll_previous);
                next_btn = (ImageButton) view.findViewById(R.id.ll_next);
                }
            else {
                prev_btn = (ImageButton) getView().findViewById(R.id.ll_previous);
                next_btn = (ImageButton) getView().findViewById(R.id.ll_next);
            }

            next_btn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    //Log.i(TAG, "You clicked next! Length: " + next_array.length + " " + next_array[0] + "/" + next_array[1] + "/" + next_array[2]);
                    buildFullTextFragment.buildFullTextFragment(next_array, offsets);
                }
            });

            prev_btn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    //Log.i(TAG, "You clicked prev!" + prev_array[0] + "/" + prev_array[1] + "/" + prev_array[2]);
                    buildFullTextFragment.buildFullTextFragment(prev_array, offsets);
                }
            });

            if (prev.isEmpty()){
                prev_btn.setAlpha(chuck_float);
                prev_btn.setOnClickListener(null);
            }
            if (next.isEmpty()){
                next_btn.setAlpha(chuck_float);
                next_btn.setOnClickListener(null);
            }

            if (getView() == null){
                View view = LayoutInflater.from(context).inflate(R.layout.full_result_linear, null);
                mTextView = (TextView) view.findViewById(R.id.full_text_title);
                mWebView = (WebView) view.findViewById(R.id.full_wv_text_result);
                }
            else {
                mTextView = (TextView) getView().findViewById(R.id.full_text_title);
                mWebView = (WebView) getView().findViewById(R.id.full_wv_text_result);
            }

            mTextView.setText(Html.fromHtml(title_string));
            final String getTOC = title_string;
            mTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i(TAG, " Get the TOC from bib citation" + getTOC);
                    String pid_query_string_match = "";
                    Pattern pid_regex = Pattern.compile("<a data-id='([^']*)'");
                    Matcher pid_match = pid_regex.matcher(getTOC);
                    if (pid_match.find()){
                        pid_query_string_match = pid_match.group(1);
                        Log.i(TAG , " PID for TOC: " + pid_query_string_match);
                        String[] pid_query_array = pid_query_string_match.split(" ");
                        buildTOCFragment.buildTOCFragment(pid_query_array);
                    }
                }
            });
            String html_header = "<html><head><link href=\"philoreader.css\" type=\"text/css\" rel=\"stylesheet\">";
            html_header = html_header.concat("<script src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js\"></script>");
            html_header = html_header.concat("<script src=\"file:///android_asset/scroll2hit.js\" type=\"text/javascript\"></script>");
            html_header = html_header.concat("<script src=\"file:///android_asset/popnote.js\" type=\"text/javascript\"></script></head>");
            //Log.i(TAG, "HEADER: " + html_header);
            results_string = "<body>" + html_header + results_string + "</body>";
            //Log.i(TAG, " Full html: " + results_string);

            mWebView.getSettings().setBuiltInZoomControls(true);
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.setWebViewClient(new com.eccoTCPAsync.app.MyWebViewClient(){
                @Override
                public void onPageFinished(WebView view, String url){
                    view.loadUrl("javascript:getOffset();");
                }
            });
            //mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null); // for nativeOnDraw error
            mWebView.loadDataWithBaseURL("file:///android_asset/", results_string, "text/html", "utf-8", "");

            Log.i(TAG, " MADE IT TO BOTTOM of FullResultFrag!!!!");

            ///// DO NOT DELETE THESE /////
            // NEED TO SEND PHILO_ID! //
            Boolean addBookmarkBoolean;
            addBookmarkBoolean = true;
            passBookmarkGoodies.passBookmarkGoodies(full_shrtcit, addBookmarkBoolean, bookmarkPhiloId2Send);

        } // end onPostExecute
    } // end async

} // end end


