package com.nopoint.midpoint.map

import android.location.Location
import android.net.Uri
import com.nopoint.midpoint.BuildConfig
import com.nopoint.midpoint.map.models.Route
import org.json.JSONObject

object Directions {
    fun buildUrl(origin: Location?, destination: String): String {
        val builder = Uri.Builder()
        builder.appendQueryParameter("origin", "${origin?.latitude},${origin?.longitude}")
            .appendQueryParameter("destination", destination)
            .appendQueryParameter("mode", "walking")
            .appendQueryParameter("key", BuildConfig.api_key)
        return builder.build().toString()
    }

    fun buildRoute(response: JSONObject): Route {
        //TODO Create models for the entire directions response & serialize it using Gson
        val routes = response.getJSONArray("routes")[0] as JSONObject
        val legs = routes.getJSONArray("legs")[0] as JSONObject
        val startLocation = legs.getJSONObject("start_location")
        val endLocation = legs.getJSONObject("end_location")
        val polyline = routes.getJSONObject("overview_polyline").getString("points")
        return Route(
            legs.getString("start_address"),
            legs.getString("end_address"),
            startLocation.getDouble("lat"),
            startLocation.getDouble("lng"),
            endLocation.getDouble("lat"),
            endLocation.getDouble("lng"),
            polyline
        )
    }
}