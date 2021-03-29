package com.example.researchproject;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TokenRequest {

    public interface ResultListener {
        public void onResult(JSONObject result);
    }

    private ResultListener listener;

    public void getAccessToken(Context context, String encodedCredentials) {
        String postUrl = "https://accounts.spotify.com/api/token";
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, postUrl, response -> {
            Log.d("MainActivity2", "access_token from the post request is: " + response);
            try {
                JSONObject jsonObject = new JSONObject(response);
                String access_token = jsonObject.getString("access_token");
                MainActivity2.setAccessToken(access_token);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            VolleyLog.d("MainActivity2", "Error: " + error.getMessage());
            error.printStackTrace();
        }) {
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("grant_type", "client_credentials");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", encodedCredentials);
                return headers;
            }
        };

        requestQueue.add(stringRequest);
    }

    public void setResultListener(ResultListener listener){
        this.listener = listener;
    }
}
