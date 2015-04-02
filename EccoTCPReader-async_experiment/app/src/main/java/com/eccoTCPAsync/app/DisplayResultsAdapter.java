package com.eccoTCPAsync.app;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class DisplayResultsAdapter extends ArrayAdapter<String>{
	
	private ArrayList<String> all_results;
	private Context context;
	//private Typeface tf;
	
	public DisplayResultsAdapter(Context context, int textViewResourceId, ArrayList<String> all_results){
		super(context, textViewResourceId, all_results);
		this.context = context;
		this.all_results = all_results;
	}

	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder holder = null;
		//Log.i("Results adapter", "getView is firing");
		String single_result = all_results.get(position);
        String[] split_result = single_result.split("<cmc>");
        String citation = split_result[0];
        //Log.i("Results adapter", citation);
        citation = citation.replaceFirst(".*</pid>","");
        String result = split_result[1];
        result = result.replaceAll("<span class=\\\"highlight\\\">([^<]*)</span>","<font color=\\\"red\\\"><b>$1</b></font>");
        result = result.replaceAll("<span>illegible</span>","*");

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
