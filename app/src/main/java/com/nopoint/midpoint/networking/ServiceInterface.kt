package com.nopoint.midpoint.networking

import org.json.JSONObject

interface ServiceInterface {
    fun post(endpoint: Endpoint, path: String, params: JSONObject, completionHandler: (response: JSONObject?) -> Unit)
    fun get(endpoint: Endpoint, path: String, completionHandler: (response: JSONObject?) -> Unit)

}