package com.MCAssignment.javafiles;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.MCAssignment.wifi.R;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class ImportDialog
{
	Activity activity;
	AlertDialog.Builder builder;
	String BSSID;

	public ImportDialog(Activity a, String BSSID)
	{
		this.activity = a;
		this.BSSID = BSSID;
		builder = new AlertDialog.Builder(a);
		builder.setCancelable(false);
	}

	public void showDialog()
	{
		builder.setTitle("Submit WiFi Password");
		builder.setMessage("Enter password");

		LayoutInflater inflater = activity.getLayoutInflater();
		final View submitView = inflater.inflate(R.layout.submit_dialog, null);
		final EditText submitDialog = (EditText) submitView.findViewById(R.id.submitDialog);

		builder.setView(submitView);
		builder.setCancelable(false);
		builder.setPositiveButton("Submit", null);
		builder.setNegativeButton("Return", null);

		final AlertDialog alert = builder.create();
		alert.show();

		alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Boolean emptyDialog = (submitDialog.getText().toString().trim().isEmpty());
				// if EditText is empty, disable closing on positive button
				if (emptyDialog) // meaning that true for the edit text is empty
				{
					submitDialog.setError("Password cannot be empty");
				}
				else
				{
					String password = submitDialog.getText().toString();
					SubmitToDB submitToDB = new SubmitToDB();
					submitToDB.execute(password, BSSID);
					alert.dismiss();
				}
			}
		});
	}

	class SubmitToDB extends AsyncTask<String, Void, String>
	{
		ProgressDialog progressDialog;

		@Override
		protected void onPreExecute()
		{
			progressDialog = new ProgressDialog(activity);
			progressDialog.setMessage("Submitting...");
			progressDialog.setCancelable(false);
			progressDialog.show();
		}

		@Override
		protected String doInBackground(String... params)
		{
			if(hasInternetAccess())
			{
				String password = params[0], BSSID = params[1], uri ="https://lkyyuen.com/mc/save.php";

				try {
					URL url = new URL(uri);
					HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
					httpURLConnection.setRequestMethod("POST");
					httpURLConnection.setDoOutput(true);
					httpURLConnection.setConnectTimeout(10000);
					httpURLConnection.setReadTimeout(10000);
					OutputStream os = httpURLConnection.getOutputStream(); // allow us to send data in the stream
					// before we read the data, need write first
					BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

					String data = URLEncoder.encode("BSSID", "UTF-8") + "=" + URLEncoder.encode(BSSID, "UTF-8") + "&" +
							URLEncoder.encode("Password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");
					bufferedWriter.write(data);
					bufferedWriter.flush();
					bufferedWriter.close();
					os.close();
					InputStream inputStream = httpURLConnection.getInputStream();
					inputStream.close();

				} catch (java.net.SocketTimeoutException e) {
                    return "Connection timeout";
					// SocketTimeoutException is a subclass of IOException.
					// however you are building a UI and you want to notify your users that a timeout occurred,
					// you must catch SocketTimeoutException before IOException, if not, it will be unreachable.
                } catch (IOException e) {
					e.printStackTrace();
				}
                return "Submitted password";
			}
			else
			{
				return "Unable to submit. Internet connection is not available";
			}
		}

		@Override
		protected void onPostExecute(String result)
		{
			progressDialog.dismiss();
			Toast.makeText(activity, result, Toast.LENGTH_LONG).show();
		}

		public boolean hasInternetAccess()
		{
			ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getActiveNetworkInfo();
			if(netInfo != null && netInfo.isConnected() && netInfo.isAvailable()) // if got connection
			{
				return true;
			}
			else
			{
				return false;
			}
		}
	}
}
