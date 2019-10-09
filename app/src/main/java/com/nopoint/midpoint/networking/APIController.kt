package com.nopoint.midpoint.networking

import org.json.JSONObject

class APIController constructor(serviceInjection: ServiceInterface):
    ServiceInterface {
    private val service: ServiceInterface = serviceInjection

    override fun post(path: String, params: JSONObject, completionHandler: (response: JSONObject?) -> Unit) {
        service.post(path, params, completionHandler)
    }

    override fun get(api: API, path: String, completionHandler: (response: JSONObject?) -> Unit) {
        service.get(api, path, completionHandler)
    }

    override fun post(path: String, params: JSONObject, auth: String, completionHandler: (response: JSONObject?) -> Unit) {
        service.post(path, params, auth, completionHandler)
    }

    override fun get(path: String, auth: String, completionHandler: (response: JSONObject?) -> Unit) {
        service.get(path, auth, completionHandler)
    }

}