package com.nopoint.midpoint.networking

import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject

class ServiceVolley : ServiceInterface {
    val TAG = ServiceVolley::class.java.simpleName

    override fun post(endpoint: Endpoint, path: String, params: JSONObject, completionHandler: (response: JSONObject?) -> Unit) {
        val url = "${endpoint.path}$path"
        Log.d("URL", url)
        val jsonObjReq = object : JsonObjectRequest(Method.POST, url, params,
            Response.Listener<JSONObject> { response ->
                Log.d(TAG, "$response")
                completionHandler(response)
            },
            Response.ErrorListener { error ->
                VolleyLog.e(TAG, " ${error.message}")
                completionHandler(null)
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }
        BackendVolley.instance?.addToRequestQueue(jsonObjReq, TAG)
    }

    override fun get(endpoint: Endpoint, path: String, completionHandler: (response: JSONObject?) -> Unit) {
        val url = "${endpoint.path}$path"
        val jsonObjReq = object : JsonObjectRequest(Method.GET, url, null,
            Response.Listener<JSONObject> { response ->
                Log.d(TAG, "/get request OK! Response: $response")
                completionHandler(response)
            },
            Response.ErrorListener { error ->
                VolleyLog.e(TAG, "/get request fail! Error: ${error.message}")
                completionHandler(null)
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }
        BackendVolley.instance?.addToRequestQueue(jsonObjReq, TAG)
    }


}