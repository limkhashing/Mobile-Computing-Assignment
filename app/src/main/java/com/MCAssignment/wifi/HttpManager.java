package com.MCAssignment.wifi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpManager
{
    public static String getData(RequestPackage p)
    {
        // uri identify the location of the feed on the web that i want to call
        BufferedReader reader = null;

        String uri = p.getUri(); // get uri from RequestPackage class

        try{
            URL url = new URL(uri);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(p.getMethod()); // get method = POST
            connection.setDoOutput(true);

            // creating writer object that can write to the output stream and send informatioon to the connection
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(p.getEncodedParams()); // getting the parameters
            writer.flush(); //ensure that anything that written on memory will be flush and send to server

            // get content from web, get 1 line at a time from remote side
            StringBuilder sb = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            //as long as there is content to read
            while((line = reader.readLine()) != null)
            {
                sb.append(line + "\n");
            }
            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            //here close reader object. takes few level
            if(reader != null)
            {
                try{
                    reader.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

}
