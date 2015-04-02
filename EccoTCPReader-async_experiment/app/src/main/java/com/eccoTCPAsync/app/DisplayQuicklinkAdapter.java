package com.eccoTCPAsync.app;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class DisplayQuicklinkAdapter extends ArrayAdapter<String>{

	private ArrayList<String> all_results;
	private Context context;
	//private Typeface tf;

	public DisplayQuicklinkAdapter(Context context, int textViewResourceId, ArrayList<String> all_results){
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

		if (view == null) {
			//tf = Typeface.createFromAsset(context.getAssets(), "fonts/FreeSans.ttf");
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.result, null);
			holder = new ViewHolder();
			view.setTag(holder);
		}
		
		holder = (ViewHolder)view.getTag();

        if (single_result != null) {
           holder.cit_view = (TextView) view.findViewById(R.id.citation_display);
           holder.cit_view.setText(Html.fromHtml(single_result));
        }
		return view;
	}
	
	static class ViewHolder {
		TextView cit_view;
	}

}
