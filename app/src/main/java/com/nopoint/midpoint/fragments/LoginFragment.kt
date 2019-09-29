package com.nopoint.midpoint.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.nopoint.midpoint.MainActivity
import com.nopoint.midpoint.R


import com.nopoint.midpoint.networking.APIController
import com.nopoint.midpoint.networking.API

import com.nopoint.midpoint.networking.ServiceVolley
import kotlinx.android.synthetic.main.fragment_login.*
import org.json.JSONException
import org.json.JSONObject


class LoginFragment : Fragment() {

    private val service = ServiceVolley()
    private val apiController = APIController(service)

    private lateinit var v: View
    private lateinit var emailField: TextInputEditText
    private lateinit var passwordField: TextInputEditText


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.fragment_login, container,false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emailField = v.findViewById(R.id.login_input_email)
        passwordField = v.findViewById(R.id.login_input_password)

        button_log_in.setOnClickListener {
            login()
        }
    }

    private fun login() {
        val params = JSONObject()

        val path = "/user/login"

        params.put("email", emailField.text.toString())
        params.put("password", passwordField.text.toString())

        apiController.post(API.LOCAL_API, path, params) { response ->
            Log.d("server response", "$response")

            val success = response!!.getString("success")

            Log.d("Succ", success)

            if (success == "true") {
                saveToken(response)

                val intent = Intent(context!!.applicationContext, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun saveToken(response: JSONObject?) {
        val editor: SharedPreferences.Editor
        val sharedPrefs: SharedPreferences =
            activity!!.getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        editor = sharedPrefs.edit()

        try {
            val token = response!!.getString("token")
            editor.putString("token", token)
            editor.apply()

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}