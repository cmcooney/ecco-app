package com.EccoTCPReaderNew.app;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class DisplayFreqAdapter extends ArrayAdapter<String>{

	private ArrayList<String> all_results;
	private Context context;

	//private Typeface tf;

	public DisplayFreqAdapter(Context context, int textViewResourceId, ArrayList<String> all_results){
		super(context, textViewResourceId, all_results);
        Log.i("Results adapter. ", "WHAT IN THE WORLD IS THE PROBLEM?");
        Log.i("Results adapter. ", all_results.toString());

		this.context = context;
        Log.i("Results adapter. ", context.toString());
		this.all_results = all_results;
	}

	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder holder = null;
		Log.i("Results adapter", "getView is firing");
		String single_result = all_results.get(position);
        String[] split_result = single_result.split("<cmc>");
        Log.i("Results adapter ", split_result[0]);
        String citation = split_result[0] + ": " + split_result[1] + " instances.";
        //citation = citation.replaceFirst(".*</pid>","");
        String result = split_result[1];
        result = result.replaceAll("<span class=\\\"highlight\\\">([^<]*)</span>","<font color=\\\"red\\\"><b>$1</b></font>");

		// http://www.ezzylearning.com/tutorial.aspx?tid=1763429 //

		if (view == null) {
			//tf = Typeface.createFromAsset(context.getAssets(), "fonts/FreeSans.ttf");
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.result, null);
			holder = new ViewHolder();
			view.setTag(holder);
		}
		
		holder = (ViewHolder)view.getTag();

        if (citation != null) {
           holder.cit_view = (TextView) view.findViewById(R.id.citation_display);
           holder.cit_view.setText(Html.fromHtml(citation));
        }
        if (result != null) {
			holder.result_view = (TextView) view.findViewById(R.id.single_result);
			holder.result_view.setText(Html.fromHtml(result));
		}
			
		return view;
	}
	
	static class ViewHolder {
		TextView result_view;
		TextView cit_view;
	}

}
