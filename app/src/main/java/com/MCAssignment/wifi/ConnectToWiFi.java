package com.MCAssignment.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ConnectToWiFi
{
    private Context context;
    private List<ScanResult> scanResultList;
    private WifiManager wifi;
    Handler handler = new Handler();
    boolean canBreak = false, connectionTimeout;

    public ConnectToWiFi(Context context)
    {
        this.context = context;
        wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public boolean getConnectionTimeout() {
        return connectionTimeout;
    }

    // true for not timed out, false for timed out
    public void setConnectionTimeout(boolean responseCode) {
        this.connectionTimeout = responseCode;
    }

    void executeCheckConnectionTimeout()
    {
        CheckConnectionTimeout checkProxy = new CheckConnectionTimeout();
        checkProxy.execute();
    }

    void connectWiFi()
    {
        scanResultList = wifi.getScanResults();
        Collections.sort(scanResultList, new Comparator<ScanResult>()
        {
            @Override
            public int compare(ScanResult lhs, ScanResult rhs)
            {
                return (lhs.level > rhs.level ? -1 : (lhs.level == rhs.level ? 0 : 1));
            }
        });
        String networkSSID;

        final WifiConfiguration conf = new WifiConfiguration();
        for(int i = 0; i < scanResultList.size(); i++)
        {
            networkSSID = scanResultList.get(i).SSID; // get from nearby SSID

            conf.SSID = "\"" + networkSSID + "\"";   // Please note the quotes. String should contain ssid in quotes
            conf.status = WifiConfiguration.Status.ENABLED;
            conf.priority = 40;

            // if is open wifi and signal strength is at good or excellent state
            if (scanResultList.get(i).level > -79 && !scanResultList.get(i).capabilities.toUpperCase().contains("WEP") && !scanResultList.get(i).capabilities.toUpperCase().contains("WPA"))
            {
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                conf.allowedAuthAlgorithms.clear();
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

                int networkId = wifi.addNetwork(conf); // -1 if failure

                if(networkId != -1)
                {
                    if(scanResultList.get(i).SSID != null)
                    {
                        wifi.disconnect(); // disconnect previous connected network
                        wifi.enableNetwork(networkId, true);
                        wifi.reconnect();

                        executeCheckConnectionTimeout();

                        handler.postDelayed(new Runnable()
                        {
                            public void run()
                            {
                                if(!getConnectionTimeout()) // if connection is timeout
                                {
                                    canBreak = false;
                                }
                                else
                                {
                                    canBreak = true;
                                }
                            }
                        }, 10000);
                    }
                }
            }
            if(canBreak)
                break;
        }
    }

    class CheckConnectionTimeout extends AsyncTask<Void, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground(Void... params) {
            return returnConnectionTimeout();
        }

        @Override
        protected void onPostExecute(Boolean timeout) {
            setConnectionTimeout(timeout);
        }

        public Boolean returnConnectionTimeout()
        {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("https://stackoverflow.com/").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(15000);
                urlc.setReadTimeout(15000);
                urlc.connect();
                return true;
            } catch (java.net.SocketTimeoutException e) {
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}
