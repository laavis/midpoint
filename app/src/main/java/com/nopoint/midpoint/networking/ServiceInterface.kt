package com.nopoint.midpoint.networking

import org.json.JSONObject

interface ServiceInterface {
    fun post(path: String, params: JSONObject, completionHandler: (response: JSONObject?) -> Unit)
    fun post(api: API, path: String, params: JSONObject, auth: String, completionHandler: (response: JSONObject?) -> Unit)
    fun get(api: API, path: String, completionHandler: (response: JSONObject?) -> Unit)
    fun get(api: API, path: String, auth: String, completionHandler: (response: JSONObject?) -> Unit)
}