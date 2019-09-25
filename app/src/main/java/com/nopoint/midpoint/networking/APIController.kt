package com.nopoint.midpoint.networking

import org.json.JSONObject

class APIController constructor(serviceInjection: ServiceInterface):
    ServiceInterface {
    private val service: ServiceInterface = serviceInjection

    override fun post(endpoint: Endpoint, path: String, params: JSONObject, completionHandler: (response: JSONObject?) -> Unit) {
        service.post(endpoint, path, params, completionHandler)
    }

    override fun get(endpoint: Endpoint, path: String, completionHandler: (response: JSONObject?) -> Unit) {
        service.get(endpoint, path, completionHandler)
    }
}