package com.nopoint.midpoint

import android.content.Context
import android.content.SharedPreferences


class SharedPref(context: Context) {
    private var mySharedPref: SharedPreferences =
        context.getSharedPreferences("filename", Context.MODE_PRIVATE)

    // this method will save the nightMode State : True or False
    fun setNightModeState(state: Boolean?) {
        val editor = mySharedPref.edit()
        editor.putBoolean("NightMode", state!!)
        editor.apply()
    }

    // this method will load the Night Mode State
    fun loadNightModeState(): Boolean? {
        return mySharedPref.getBoolean("NightMode", false)
    }

    // Get arrived radius
    fun getArrivedRadius(): Double {
        return mySharedPref.getFloat("arrivedRadius", 10.0f).toDouble()
    }

    // Get places API search radius
    fun getPlacesRadius(): String {
        return mySharedPref.getString("placesRadius", "400") ?: "400"
    }

    fun setArrivedRadius(radius: Double) {
        val editor = mySharedPref.edit()
        editor.putFloat("arrivedRadius", radius.toFloat())
        editor.apply()
    }

    fun setPlacesRadius(radius: String) {
        val editor = mySharedPref.edit()
        editor.putString("placesRadius", radius)
        editor.apply()
    }
}