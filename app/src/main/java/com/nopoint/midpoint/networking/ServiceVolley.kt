package com.nopoint.midpoint.networking

import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject

class ServiceVolley : ServiceInterface {
    val TAG = ServiceVolley::class.java.simpleName

    override fun post(
        api: API,
        path: String,
        params: JSONObject,
        completionHandler: (response: JSONObject?) -> Unit
    ) {
        val url = "${api.path}$path"
        Log.d("URL", url)
        val jsonObjReq = object : JsonObjectRequest(Method.POST, url, params,
            Response.Listener<JSONObject> { response ->
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

    override fun get(api: API, path: String, completionHandler: (response: JSONObject?) -> Unit) {
        val url = "${api.path}$path"
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

    override fun post(
        api: API,
        path: String,
        params: JSONObject,
        auth: String,
        completionHandler: (response: JSONObject?) -> Unit
    ) {
        val url = "${api.path}$path"
        Log.d("URL", url)
        val jsonObjReq = object : JsonObjectRequest(Method.POST, url, params,
            Response.Listener<JSONObject> { response ->
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
                headers["authorization"] = auth
                return headers
            }
        }
        BackendVolley.instance?.addToRequestQueue(jsonObjReq, TAG)
    }

    override fun get(
        api: API,
        path: String,
        auth: String,
        completionHandler: (response: JSONObject?) -> Unit
    ) {
        val url = "${api.path}$path"
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
                headers["authorization"] = auth
                return headers
            }
        }
        BackendVolley.instance?.addToRequestQueue(jsonObjReq, TAG)
    }
}