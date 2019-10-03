package com.nopoint.midpoint.map

import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import com.nopoint.midpoint.BuildConfig

object PlacesUtils {

    fun buildUrl(location: LatLng, type: String): String {
        val builder = Uri.Builder()
        builder.appendQueryParameter("location", "${location.latitude},${location.longitude}")
            .appendQueryParameter("type", type) // https://developers.google.com/places/web-service/supported_types
            .appendQueryParameter("radius", "400") // meters
            .appendQueryParameter("key", BuildConfig.api_key)
        return builder.build().toString()
    }
}