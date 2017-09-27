package com.MCAssignment.wifi;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class RequestPackage
{
    // uri is the address i send the request
    // GET request = parameters are appending to uri
    // POST request = parameters are place into the body of request
    private String uri, method = "POST";

    // set up a way to contain the parameters
    private Map<String,String> params = new HashMap<>();

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getParams() {
        return params;
    }

    //params for object
    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    // param is for instead having application set all the parameters at the same time
    // i only allow it set 1 parameters at a time. for easier to use
    public void setParam(String key, String value)
    {
        // so when i call this method from anywhere in the app im adding the parameters to the package
        params.put(key, value);
    }

    // allow to retrieve parameters as encoded string
    // need encoded the string because if space bar, browser will change it
    public String getEncodedParams()
    {
        // this will return a string
        StringBuilder sb = new StringBuilder();

        // map will contains 2 parameters
        // key is for parameters
        // to get those keys, params.keySet()
        // keySet() is to return a set of key of all the params and can loop through those
        for(String key : params.keySet())
        {
            // constructor with only 1 argv is deprecated
            // the first argv is the value, value that get from params map
            // second argv is literal string, character set name
            String value = null;
            try {
                value = URLEncoder.encode(params.get(key), "UTF-8"); // now have a key and a value
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            //if this is a first time in a loop, the string would be empty and dont need to add
            // anything to it. But all subsequent character need
            if(sb.length() > 0) // only execute this is string has something on it
            {
                sb.append("&");
            }
            sb.append(key + "=" + value);
        }
        return sb.toString();
    }

}
