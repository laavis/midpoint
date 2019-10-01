package com.nopoint.midpoint.models

import android.content.Context
import android.util.Log
import com.google.gson.Gson

object CurrentUser {
    fun getCurrentUser(context: Context): User? {
        val prefs = context.getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        val jsonUser = prefs.getString("localUser", null)
        return if (jsonUser != null) {
            val localUser = Gson().fromJson(jsonUser, LocalUser::class.java)
            //Check if user session is still valid
            if (System.currentTimeMillis() < localUser.expiration) localUser.user else null
        } else null
    }

    fun getToken(context: Context): String? {
        val prefs = context.getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        val jsonUser = prefs.getString("localUser", null)
        return if (jsonUser != null) {
            val localUser = Gson().fromJson(jsonUser, LocalUser::class.java)
            //Check if user session is still valid
            if (System.currentTimeMillis() < localUser.expiration) localUser.token else null
        } else null
    }

    fun getLocalUser(context: Context): LocalUser? {
        val prefs = context.getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        val jsonUser = prefs.getString("localUser", null)
        return if (jsonUser != null) {
            val localUser = Gson().fromJson(jsonUser, LocalUser::class.java)
            //Check if user session is still valid
            if (System.currentTimeMillis() < localUser.expiration) localUser else null
        } else null
    }
}