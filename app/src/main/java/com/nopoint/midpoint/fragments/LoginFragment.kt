package com.nopoint.midpoint.fragments

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.iid.FirebaseInstanceId
import com.nopoint.midpoint.MainActivity
import com.nopoint.midpoint.R
import com.nopoint.midpoint.networking.APIController
import com.nopoint.midpoint.networking.API
import com.nopoint.midpoint.networking.ServiceVolley
import kotlinx.android.synthetic.main.fragment_login.*
import com.google.gson.Gson
import com.nopoint.midpoint.EntryActivity
import com.nopoint.midpoint.models.LocalUser
import com.nopoint.midpoint.models.LoginErrorResponse
import com.nopoint.midpoint.models.LoginResponse
import com.nopoint.midpoint.models.User
import com.nopoint.midpoint.networking.LOGIN
import kotlinx.android.synthetic.main.activity_entry.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.lang.Error
import java.lang.Exception

class LoginFragment : Fragment() {

    private val service = ServiceVolley()
    private val apiController = APIController(service)

    private lateinit var v: View
    private lateinit var emailField: TextInputEditText
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordField: TextInputEditText
    private lateinit var passwordLayout: TextInputLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.fragment_login, container,false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setSpacing()

        emailField = v.findViewById(R.id.login_input_email)
        emailLayout = v.findViewById(R.id.login_input_layout_email)
        passwordField = v.findViewById(R.id.login_input_password)
        passwordLayout = v.findViewById(R.id.login_input_layout_password)

        button_log_in.setOnClickListener {
            login()
        }
    }

    private fun setSpacing() {
        val displayMetrics = DisplayMetrics()
        (activity as EntryActivity).windowManager.defaultDisplay.getMetrics(displayMetrics)

        val height = displayMetrics.heightPixels
        val containerPaddingTop = if (height < 2400) (height * 0.2).toInt() else (height * 0.25).toInt()

        login_container.setPadding(0, containerPaddingTop, 0, 0)
    }

    private fun login() {
        val body = JSONObject()

        body.put("email", emailField.text.toString())
        body.put("password", passwordField.text.toString())

        apiController.post(LOGIN, body) { response ->
            try {

                if (response == null) {
                    throw Exception("Failed to connect")
                }

                val loginResponse = Gson().fromJson(response.toString(), LoginResponse::class.java)

                Log.d("FIRE", "$loginResponse")


                if (loginResponse.success) {
                    saveTokenAndUser(loginResponse.token, loginResponse.user)
                    updateFirebaseToken(loginResponse.token)
                    val intent = Intent(context!!.applicationContext, MainActivity::class.java)
                    startActivity(intent)
                    (activity as EntryActivity).finish()
                } else {
                    val authErrorResponse = Gson().fromJson(response.toString(), LoginErrorResponse::class.java)
                    setErrors(authErrorResponse)
                }

            } catch (e: Exception) {
                Log.e("LOGIN", "$e")
                Toast.makeText(context, "${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateFirebaseToken(userToken: String) {
        Log.d("FIRE", "sakdjsak")
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FIRBASE", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }
                // Get new Instance ID token
                val token = task.result?.token

                // Log and toast
                val msg = "TOKEN: $token"
                val path = "/user/updateToken"
                val body = JSONObject()
                body.put("firebaseToken", token)
                Log.d("FIRE", "TOKEN ON TÄSÄ: $token")

                apiController.post(API.LOCAL_API, path, body, userToken) { response ->
                    Log.d("FIRE", "$response")
                    val message = response?.optString("msg") ?: response?.optString("errors")
                    Log.d("FIRBASE TOKEN MAP", "$message")
                }
                Log.d(ContentValues.TAG, msg)
            })

    }

    private fun setErrors(authErrRes: LoginErrorResponse) {
        emailLayout.error = authErrRes.email
        passwordLayout.error = authErrRes.password
    }


    private fun saveTokenAndUser(token: String, user: User) {
        val editor: SharedPreferences.Editor
        val sharedPrefs: SharedPreferences =
            activity!!.getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        editor = sharedPrefs.edit()
        val expiration: Long = System.currentTimeMillis() + 3600000 //1 hour

        val localUser = LocalUser(user, token, expiration)
        try {
            val serializedUser = Gson().toJson(localUser)
            editor.putString("localUser", serializedUser)
            editor.apply()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}