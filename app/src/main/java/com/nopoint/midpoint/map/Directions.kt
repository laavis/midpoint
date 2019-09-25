package com.nopoint.midpoint.map

import android.location.Location
import android.net.Uri
import com.nopoint.midpoint.BuildConfig
import com.nopoint.midpoint.map.models.Route
import org.json.JSONObject

object Directions {
    /**
     * Builds url for Directions API based on location name
     * @param origin Starting point for the route
     * @param destination Name of the location to get the route to
     * @return String with the parameters for the directions API GET request
     */
    fun buildUrl(origin: Location?, destination: String): String {
        val builder = Uri.Builder()
        builder.appendQueryParameter("origin", "${origin?.latitude},${origin?.longitude}")
            .appendQueryParameter("destination", destination)
            .appendQueryParameter("mode", "walking")
            .appendQueryParameter("key", BuildConfig.api_key)
        return builder.build().toString()
    }

    /**
     * Builds url for Directions API based on location name
     * @param origin Starting point for the route
     * @param destination Location object with the coordinates for the destination
     * @return String with the parameters for the directions API GET request
     */
    fun buildUrl(origin: Location?, destination: Location): String {
        val builder = Uri.Builder()
        builder.appendQueryParameter("origin", "${origin?.latitude},${origin?.longitude}")
            .appendQueryParameter("destination", "${destination.latitude},${destination.longitude}")
            .appendQueryParameter("mode", "walking")
            .appendQueryParameter("key", BuildConfig.api_key)
        return builder.build().toString()
    }

    /**
     * Builds route object from directions API json response
     * @param response The google directions API response json object
     * @return Route object with coordinates for start & end and a polyline of the route
     */
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

    /**
     * Calculates & returns the middle point between 2 coordinates
     * @param start Location object with the starting coordinates
     * @param end Location object with the destination coordinates (e.g. the friend's coordinates)
     * @return Location object with the exact middle point between the start & end
     */
    fun getMiddlePoint(start: Location, end: Location): Location {
        val midLat = (start.latitude + end.latitude) / 2
        val midLng = (start.longitude + end.longitude) / 2
        val location = Location("")
        location.latitude = midLat
        location.longitude = midLng
        return location
    }
}