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
import com.google.android.material.textfield.TextInputLayout
import com.nopoint.midpoint.MainActivity
import com.nopoint.midpoint.R
import com.nopoint.midpoint.networking.APIController
import com.nopoint.midpoint.networking.API
import com.nopoint.midpoint.networking.ServiceVolley
import kotlinx.android.synthetic.main.fragment_login.*
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class LoginFragment : Fragment() {

    private val service = ServiceVolley()
    private val apiController = APIController(service)

    private lateinit var v: View
    private lateinit var emailField: TextInputEditText
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordField: TextInputEditText
    private lateinit var passwordLayout: TextInputLayout

    data class LoginResponse(
        @SerializedName("success")val success: Boolean,
        @SerializedName("user")val user: UserLogin,
        @SerializedName("token")val token: String
    )

    data class UserLogin (
        @SerializedName("_id")val id: String,
        @SerializedName("username")val username: String,
        @SerializedName("email")val email: String,
        @SerializedName("password")val password: String,
        @SerializedName("friendsList")val friendsList: ArrayList<String>
    )

    data class LoginErrorResponse (
        @SerializedName("email")val email: String,
        @SerializedName("password")val password: String
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.fragment_login, container,false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emailField = v.findViewById(R.id.login_input_email)
        emailLayout = v.findViewById(R.id.login_input_layout_email)
        passwordField = v.findViewById(R.id.login_input_password)
        passwordLayout = v.findViewById(R.id.login_input_layout_password)

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
            try {
                val loginResponse = Gson().fromJson(response.toString(), LoginResponse::class.java)

                Log.d("RES", "$loginResponse")

                if (loginResponse.success) {
                    saveToken(loginResponse.token)

                        val intent = Intent(context!!.applicationContext, MainActivity::class.java)
                        startActivity(intent)
                } else {
                    val loginErrorResponse = Gson().fromJson(response.toString(), LoginErrorResponse::class.java)
                    setErrors(loginErrorResponse)
                }

            } catch (e: IOException) {
                Log.e("LOGIN", "$e")
            }
        }
    }

    private fun setErrors(ler: LoginErrorResponse) {
        emailLayout.error = ler.email
        passwordLayout.error = ler.password
    }


    private fun saveToken(token: String) {
        val editor: SharedPreferences.Editor
        val sharedPrefs: SharedPreferences =
            activity!!.getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        editor = sharedPrefs.edit()

        try {
            editor.putString("token", token)
            editor.apply()

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}