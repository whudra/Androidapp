package com.example.com.networkclient;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class SerialRequest extends StringRequest {
    final static private String URL = "http://192.168.86.252:9001/AndroidConnect.jsp";
    private Map<String, String> param;

    public SerialRequest(String serial, Response.Listener<String> listener){
        super(Request.Method.POST, URL, listener, null);
        param = new HashMap<>();
        param.put("serialNumber", serial);
    }

    public Map<String, String> getParam(){
        return param;
    }
}
