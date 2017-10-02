package com.MCAssignment.wifi;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.MCAssignment.adapter.ListAdapter;
import com.MCAssignment.javafiles.ImportDialog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends Activity
{
	Button setWifi;
	WifiManager wifiManager;
	WifiReceiver receiverWifi;
	List<ScanResult> wifiList;
	List<String> listOfProvider;
	ListAdapter adapter;
	ListView listViwProvider;
	RequestPackage p;
	ProgressDialog progressDialog;
	Handler handler = new Handler();
	CheckInternetConnection checkInternetConnection;
	ConnectToWiFi connectToWiFi;
	String[] PasswordMessage;
	int count = 0;
	final String uri = "https://lkyyuen.com/mc/pswd.php";
	ClipData myClip;
	ClipboardManager clipboard;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// instantiate variable
		listOfProvider = new ArrayList<>();
		progressDialog = new ProgressDialog(MainActivity.this);
		p = new RequestPackage();
		checkInternetConnection = new CheckInternetConnection(this);
		connectToWiFi = new ConnectToWiFi(this);
		clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

		// references to layout
		listViwProvider = (ListView) findViewById(R.id.listViewWiFi);
		setWifi = (Button) findViewById(R.id.wifiButton);

		listViwProvider.setVisibility(ListView.GONE);

		//wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

		p.setMethod("POST"); // for document the nature of call
		p.setUri(uri);

		// set the functionality of ON/OFF button
		setWifi.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (wifiManager.isWifiEnabled()) // if wifi is enabled, turn it off ( ON -> OFF )
				{
					wifiManager.setWifiEnabled(false);
					setWifi.setText("Turn On WiFi");
					listViwProvider.setVisibility(ListView.GONE);
					Toast.makeText(MainActivity.this, "Disabled Wi-Fi", Toast.LENGTH_SHORT).show();
					count = 0; // reset
				}
				else if (!wifiManager.isWifiEnabled()) // if wifi is disabled, turn it on ( OFF -> ON )
				{
					wifiManager.setWifiEnabled(true);
					setWifi.setText("Turn Off Wifi");
					Toast.makeText(MainActivity.this, "Enabled Wi-Fi", Toast.LENGTH_SHORT).show();
					//after on wifi, total wait 30 sec to do everything
					progressDialog.setMessage("Checking for Wi-Fi connection");
					progressDialog.setCancelable(false);
					progressDialog.show();
					handler.postDelayed(new Runnable() // here 10 sec
					{
						public void run()
						{
							checkInternetConnection.executeCheckInternet();
							checkConnectivity();
						}
					}, 10000);
				}
			}
		});

		// check wifi enable or not
		// if wifi is enable, start searching available wifi
		if (wifiManager.isWifiEnabled())
		{
			setWifi.setText("Turn Off WiFi");
			Toast.makeText(MainActivity.this, "WiFi is enabled", Toast.LENGTH_SHORT).show();
			progressDialog.setMessage("Checking for Wi-Fi connection");
			progressDialog.setCancelable(false);
			progressDialog.show();
			checkInternetConnection.executeCheckInternet();
			checkConnectivity();
		}
		else
		{
			setWifi.setText("Turn On WiFi");
			listViwProvider.setVisibility(ListView.GONE);
		}

		// opening a submit password dialog on click
		listViwProvider.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				ImportDialog action = new ImportDialog(MainActivity.this, (wifiList.get(position)).BSSID);
				action.showDialog();
			}
		});

		// long click to copy password
		listViwProvider.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id)
			{
				if(PasswordMessage[pos].equals(" ") || PasswordMessage[pos].trim().equals("No Password Available"))
				{
					Toast.makeText(MainActivity.this, "No password to copy", Toast.LENGTH_LONG).show();
				}
				else
				{
					myClip = ClipData.newPlainText("password", PasswordMessage[pos].trim());
					Toast.makeText(MainActivity.this, "Copied password", Toast.LENGTH_LONG).show();
					clipboard.setPrimaryClip(myClip);
				}
				return true;
			}
		});
	}

	private void scanning()
	{
		// wifi scanned value broadcast receiver
		receiverWifi = new WifiReceiver();
		// Register broadcast receiver
		// Broadcast receiver will automatically call when number of wifi connections changed
		registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		wifiManager.startScan();
	}

	protected void onPause()
	{
		super.onPause();
		if(receiverWifi != null)
		{
			unregisterReceiver(receiverWifi);
		}
	}

	protected void onResume()
	{
		registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		super.onResume();
	}

	public void refreshList(View view)
	{
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (wifiManager.isWifiEnabled())
        {
            if(netInfo != null && netInfo.isConnected() && netInfo.isAvailable()) // if got connection
            {
                if(connectToWiFi.getConnectionTimeout() && checkInternetConnection.isConnectionResult())
                {
                    MyTask task = new MyTask();
                    task.execute();
                }
                else
                {
                    setWifi.setText("Turn Off WiFi");
                    wifiManager.setWifiEnabled(true);
                    progressDialog.setMessage("Checking for Wi-Fi connection");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    handler.postDelayed(new Runnable() // wait 15 sec
                    {
                        public void run()
                        {
                            checkInternetConnection.executeCheckInternet();
                            checkConnectivity();
                        }
                    }, 15000);
                }
            }
            else
            {
                Toast.makeText(MainActivity.this, "Can't refresh. No internet connection", Toast.LENGTH_LONG).show();
            }
        }
        else
        {
            Toast.makeText(MainActivity.this, "Please enable WiFi first", Toast.LENGTH_LONG).show();
        }

	}

	public void checkConnectivity()
	{
		connectToWiFi.executeCheckConnectionTimeout();
		handler.postDelayed(new Runnable() // here wait 10 seconds
		{
			public void run()
			{
				if(connectToWiFi.getConnectionTimeout() && checkInternetConnection.isConnectionResult())
				{
					progressDialog.dismiss();
					Toast.makeText(MainActivity.this, "Internet connection is available", Toast.LENGTH_LONG).show();
					MyTask task = new MyTask();
					task.execute();
				}
				else
				{
					progressDialog.dismiss();
					if(count == 0)
					{
						progressDialog.setMessage("No WiFi Connection. Trying to connect to nearby public WiFi");
						progressDialog.setCancelable(false);
						progressDialog.show();
						connectToWiFi.connectWiFi();
						count++;
						checkInternetConnection.executeCheckInternet();
						connectToWiFi.executeCheckConnectionTimeout();
						checkConnectivity();
					}
					else if(count == 1)
					{
						progressDialog.setMessage("There is no suitable WiFi to connect. Enabling mobile data...");
						progressDialog.setCancelable(false);
						progressDialog.show();

						// check android version here
						// In Android Lollipop, setMobileDataEnabled API is removed from ConnectivityManager and moved to TelephonyManager
						// and only work for rooted phone
						// There is no official way to do this. However, it can be achieved unofficially with reflection.
						// force open data does not work above android 4.4, instead ask the user to enable data
						try {
							setMobileDataEnabled(true);
						} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
							e.printStackTrace();
						} finally {
							handler.postDelayed(new Runnable() // here wait 10 seconds after data enabled to check connectivity
							{
								public void run() {
									count++;
									Toast.makeText(MainActivity.this, "Enabled mobile data. Checking connection...", Toast.LENGTH_LONG).show();
									checkInternetConnection.executeCheckInternet();
									connectToWiFi.executeCheckConnectionTimeout();
									checkConnectivity();
								}
							}, 10000);
						}
					}
					else
					{
						wifiList = wifiManager.getScanResults();
						PasswordMessage = new String[wifiList.size()]; // instantiate PasswordMessage
						for(int i = 0; i < wifiList.size(); i++)
						{
							PasswordMessage[i] = " ";
						}
						listViwProvider.setVisibility(ListView.VISIBLE);
						AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
						builder.setCancelable(false);
						builder.setTitle("No internet connection");
						builder.setMessage("Can't get data from Internet");
						builder.setPositiveButton("Return", new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int id)
							{
								dialog.cancel();
								scanning();
							}
						});
						AlertDialog alert = builder.create();
						alert.show();
					}
				}
			}
		}, 10000);
	}

	private void setMobileDataEnabled(boolean enabled) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
	{
		// if enable is false, data is disable
		final ConnectivityManager conman = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
		Method dataMtd = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
		dataMtd.setAccessible(enabled);
		dataMtd.invoke(conman, enabled);
	}

	class WifiReceiver extends BroadcastReceiver
	{
		// This method will be call when number of wifi connections changed
		public void onReceive(Context c, Intent intent)
		{
			// sorting of wifi based on signal strength
			Collections.sort(wifiList, new Comparator<ScanResult>()
			{
				@Override
				public int compare(ScanResult lhs, ScanResult rhs)
				{
					return (lhs.level > rhs.level ? -1 : (lhs.level == rhs.level ? 0 : 1));
				}
			});

			listOfProvider.clear();
			String wifiProvider, signalStrength;

			// get their information
			for (int i = 0; i < wifiList.size(); i++)
			{
				// set signal strength. because negative. more to 0 is more larger
				if((wifiList.get(i).level ) > -60)
				{
					signalStrength = "Excellent";
				}
				else if((wifiList.get(i).level ) > -79)
				{
					signalStrength = "Good";
				}
				else
				{
					signalStrength = "Weak";
				}

				wifiProvider =  "SSID : " + wifiList.get(i).SSID  + "\n" +
						"BSSID : " + wifiList.get(i).BSSID + "\n" +
						"Signal Strength : " + signalStrength + "\n" +
						"Password : " + PasswordMessage[i];
				listOfProvider.add(wifiProvider);
			}

			// setting list of all wifi provider in a List
			adapter = new ListAdapter(MainActivity.this, listOfProvider);
			listViwProvider.setAdapter(adapter);
			
			adapter.notifyDataSetChanged();
		}
	}

	//async task will send bssid to server, run php script into run query
	private class MyTask extends AsyncTask<Void, Void, String>
	{
		String[] passwordFromPHP;
		@Override
		protected void onPreExecute()
		{
			wifiList = wifiManager.getScanResults();
			PasswordMessage = new String[wifiList.size()]; // instantiate PasswordMessage
			// passwordFromPHP to store each result after check DB
			passwordFromPHP = new String[wifiList.size()];

			progressDialog.setMessage("Fetching data...");
			progressDialog.setCancelable(false);
			progressDialog.show();
		}

		@Override
		protected String doInBackground(Void... Void)
		{
			Collections.sort(wifiList, new Comparator<ScanResult>()
			{
				@Override
				public int compare(ScanResult lhs, ScanResult rhs)
				{
					return (lhs.level > rhs.level ? -1 : (lhs.level == rhs.level ? 0 : 1));
				}
			});

			// when doinbackground is called, that uri passed as first parameters
			// here pass BSSID to check, keep send to PHP for query BSSID 1 by 1 then store into array
			for(int i = 0; i < wifiList.size(); i++)
			{
				p.setParam("BSSID", wifiList.get(i).BSSID); // KEY follow PHP, BSSID as value
				passwordFromPHP[i] = HttpManager.getData(p);
			}
			System.arraycopy(passwordFromPHP, 0, PasswordMessage, 0, PasswordMessage.length);
			return "Finished fetch";
		}

		@Override
		protected void onPostExecute(String result)
		{
			progressDialog.dismiss();
			Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
			listViwProvider.setVisibility(ListView.VISIBLE);
			scanning();
		}
	}
}
