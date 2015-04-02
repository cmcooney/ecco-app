package com.eccoTCPAsync.app;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class UrlConnect extends AsyncTask<String, Void, Boolean>{
	public int response_code = 0;
	public String response_message = "";
	private static final String TAG = "URLConnect";
	Boolean live_server = false;
	Boolean catch_exception = false;
	public Exception exception;
	ConnectionDetector cd;
	Boolean isInternetPresent = false;
	//SearchFunctions sf;
	ProgressDialog dialog;


	@Override
	protected void onPreExecute() {
		Log.e(TAG, "URLCON PreExecute");
	}
	
	@Override
	protected Boolean doInBackground(String... urls) {
		
		try {
			Log.e(TAG, "TRYING TO CONNECT TO CONDORCET");
			
			URL url = new URL("http://condorcet.uchicago.edu");
			//URL url = new URL("http://barthes.uchicago.edu");
			HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
			urlc.setReadTimeout(10000);
			urlc.setConnectTimeout(15000);
			urlc.setRequestMethod("GET");
			urlc.setDoInput(true);
			response_message = urlc.getResponseMessage();
			response_code = urlc.getResponseCode();
			
			Log.e(TAG, "HTTP response: " + response_code + " " + response_message);
			
			
			
			//if (response_code == HttpURLConnection.HTTP_OK) {
			if (response_code > 0) {
				Log.e(TAG, "Boolean live connection condition is true");
				//live_server = true;
				//return live_server;
				return true;
			}
			
			else {
				Log.e(TAG, "Response code HTTP_OK is NOT okay!");
				//live_server=false;
				//return live_server;
				return false;
			}
			
			
		}
		catch (MalformedURLException e1) {
			return false;
		}
		catch (IOException exception){
			Log.e(TAG, "I'm in the catch IOException "+ exception.toString());
			catch_exception = true;
			//return catch_exception;
			return false;
		}
		
	}
	protected void onPostExecute(){
	}
}
