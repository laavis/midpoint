package com.nopoint.midpoint.map

import android.location.Location
import android.net.Uri
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.nopoint.midpoint.BuildConfig
import com.nopoint.midpoint.map.models.Direction
import com.nopoint.midpoint.map.models.FullRoute
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

    fun buildUrlTest(origin: LatLng, destination: LatLng?): String {
        val builder = Uri.Builder()
        builder.appendQueryParameter("origin", "${origin.latitude},${origin.longitude}")
            .appendQueryParameter("destination", "${destination!!.latitude},${destination.longitude}")
            .appendQueryParameter("mode", "walking")
            .appendQueryParameter("key", BuildConfig.api_key)
        return builder.build().toString()
    }

    /**
     * Builds route object from directions API json response
     * @param response The google directions API response json object
     * @return FullRoute object with coordinates for start & end and a polyline of the route
     */
    fun buildRoute(response: JSONObject): FullRoute {
        val directions = Gson().fromJson(response.toString(), Direction::class.java)
        val bestRoute = directions.routes[0]
        val leg = bestRoute.legs[0]
        return FullRoute(
            leg.start_address,
            leg.end_address,
            leg.start_location.lat,
            leg.start_location.lng,
            leg.end_location.lat,
            leg.end_location.lng,
            bestRoute.overview_polyline.points)
    }

    data class MidPointRoute(
        val startLat: Double,
        val startLng: Double,
        val endLat: Double,
        val endLng: Double
    )


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

        Log.d("DIRECTIONS", "lat: ${location.latitude}, lng: ${location.longitude}")
        return location
    }

}