package com.example.researchproject;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class GetRequest {

    public interface ResultListener {
        public void onResult(JSONObject result);
    }

    private ResultListener listener;

    public void volleyGet(Context context, String search_type, String input, String access_token) throws UnsupportedEncodingException {
        String url;

        if (search_type.equals("Search Album")) {
            String encodedText = URLEncoder.encode(input, "UTF-8").replace("+", "%20");
            url = "https://api.spotify.com/v1/search?type=album&q=" + encodedText;
        } else {
            url = "https://api.spotify.com/v1/albums/" + input + "/tracks";
        }

        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            Log.d("GetRequest", response.toString());
        }, error -> error.printStackTrace())
        {
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                String auth = "Bearer " + access_token;
                headers.put("Authorization", auth);
                return headers;
            }
        };

        requestQueue.add(jsonObjectRequest);
    }

    public void setResultListener(ResultListener listener){
        this.listener = listener;
    }
}
