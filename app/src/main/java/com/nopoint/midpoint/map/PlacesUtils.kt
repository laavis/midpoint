package com.nopoint.midpoint.map

import android.net.Uri
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.nopoint.midpoint.BuildConfig

object PlacesUtils {

    fun buildUrl(location: LatLng, radius: String): String {
        val builder = Uri.Builder()
        builder.appendQueryParameter("location", "${location.latitude},${location.longitude}")
            .appendQueryParameter("type", "cafe") // https://developers.google.com/places/web-service/supported_types
            .appendQueryParameter("radius", radius) // meters
            .appendQueryParameter("key", BuildConfig.api_key)
        return builder.build().toString()
    }
}