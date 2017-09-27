package com.MCAssignment.wifi;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CheckInternetConnection
{
    boolean connectionResult;
    Context context;
    ProgressDialog progressDialog;
    Executor executor = Executors.newSingleThreadExecutor();

    public CheckInternetConnection(Context context)
    {
        this.context = context;
        progressDialog = new ProgressDialog(context);
    }

    public void setConnectionResult(boolean connectionResult)
    {
        this.connectionResult = connectionResult;
    }

    public boolean isConnectionResult() {
        return connectionResult;
    }

    public void executeCheckInternet()
    {
        CheckInternet checkInternet = new CheckInternet();
        checkInternet.execute();
    }

    class CheckInternet extends AsyncTask<Void, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground(Void... params)
        {
            if(hasInternetAccess())
            {
                try {
                    int timeoutMs = 1500;
                    Socket sock = new Socket();
                    SocketAddress sockaddr = new InetSocketAddress("8.8.8.8", 53);
                    sock.connect(sockaddr, timeoutMs);
                    sock.close();
                    return true;
                } catch (IOException e) {
                    return false;
                }
            }
            else
            {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            setConnectionResult(result);
        }

        public boolean hasInternetAccess()
        {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
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
