package com.nopoint.midpoint.map

import android.location.Location
import android.net.Uri
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import com.nopoint.midpoint.BuildConfig
import com.nopoint.midpoint.map.models.Direction
import com.nopoint.midpoint.map.models.FullRoute
import com.nopoint.midpoint.models.MeetingRequest
import com.nopoint.midpoint.models.MeetingRequestRow
import com.nopoint.midpoint.models.Result
import com.nopoint.midpoint.models.RowType
import org.json.JSONObject

object DirectionsUtils {

    /**
     * Builds url for DirectionsUtils API based on location name
     * @param origin LatLng object with the starting point for the route
     * @param destination LatLng object with the coordinates for the destination
     * @return String with the parameters for the directions API GET request
     */
    fun buildUrlFromLatLng(origin: LatLng, destination: LatLng?): String {
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
    fun buildRoute(response: JSONObject, meetingPlace: String? = null): FullRoute {
        val directions = Gson().fromJson(response.toString(), Direction::class.java)
        val bestRoute = directions.routes[0]
        val leg = bestRoute.legs[0]
        return FullRoute(
            leg.start_address,
            meetingPlace ?: leg.end_address,
            leg.start_location.lat,
            leg.start_location.lng,
            leg.end_location.lat,
            leg.end_location.lng,
            bestRoute.overview_polyline.points)
    }

    /**
     * Builds jsonObject with the parameters required
     * @param
     * @return JsonObject with the given params
     */
    fun buildResponseBody(meetingRequest: MeetingRequest, latestLocation: LatLng, midpointLatLng: LatLng): JSONObject {
        val body = JSONObject()
        body.put("requestId", meetingRequest.id)
        body.put("lat", latestLocation.latitude)
        body.put("lng", latestLocation.longitude)
        body.put("middleLat", midpointLatLng.latitude)
        body.put("middleLng", midpointLatLng.longitude)
        body.put("response", 1) //TODO allow setting different response types
        return body
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

        Log.d("DIRECTIONS", "lat: ${location.latitude}, lng: ${location.longitude}")
        return location
    }

    fun getAbsoluteMidpoint(result: Direction): LatLng? {
        Log.d("MEETING", "$result")
        val legs = result.routes[0].legs

        val path = ArrayList<LatLng>()

        for (i in 0 until legs[0].steps.size) {
            path.addAll(PolyUtil.decode(legs[0].steps[i].polyline.points))
        }

        // Get full distance between two points in meters
        val midpointInMeters = (legs[0].distance.value / 2).toDouble()

        // Calculate path's midpoint lat/lng from path's first point
        return calculateMidpointCoordinates(path, path[0], midpointInMeters)
    }

    private fun calculateMidpointCoordinates(path: List<LatLng>, origin: LatLng, distance: Double): LatLng? {
        var midpointCoordinates: LatLng? = null

        // Check if given origin coordinates are found in the path's coordinates
        // withing five meters
        if (!PolyUtil.isLocationOnPath(origin, path, false, 1.0)) {
            return null
        }

        var accDistance = 0.0
        val segment = ArrayList<LatLng>()

        // Loop through path list
        for (i in 0 until path.size - 1) {
            val segmentStart = path[i]
            val segmentEnd = path[i + 1]

            segment.clear()
            segment.add(segmentStart)
            segment.add(segmentEnd)

            var currentDistance: Double

            // Calculate length of a segment
            currentDistance = SphericalUtil.computeDistanceBetween(segmentStart, segmentEnd)

            // Current distance goes over given distance, midpoint found,
            // fix offset and set midpoint coordinates
            if (currentDistance + accDistance > distance) {
                val heading: Double = SphericalUtil.computeHeading(segmentStart, segmentEnd)
                midpointCoordinates = SphericalUtil.computeOffset(segmentStart, distance - accDistance, heading)
                break
            }

            accDistance += currentDistance
        }
        return midpointCoordinates
    }

}