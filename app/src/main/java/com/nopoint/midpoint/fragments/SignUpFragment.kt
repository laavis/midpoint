package com.nopoint.midpoint.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.nopoint.midpoint.R
import com.nopoint.midpoint.networking.APIController
import com.nopoint.midpoint.networking.Endpoint
import com.nopoint.midpoint.networking.ServiceVolley
import kotlinx.android.synthetic.main.fragment_sign_up.*
import org.jetbrains.anko.doAsync
import org.json.JSONObject
import java.io.IOException

class SignUpFragment : Fragment() {

    private val service = ServiceVolley()
    private val apiController = APIController(service)

    private lateinit var v: View
    private lateinit var emailField: TextInputEditText
    private lateinit var emailLayout: TextInputLayout
    private lateinit var usernameField: TextInputEditText
    private lateinit var usernameLayout: TextInputLayout
    private lateinit var passwordField: TextInputEditText
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var confirmPasswordField: TextInputEditText
    private lateinit var confirmPasswordLayout: TextInputLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.fragment_sign_up, container,false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        emailField = v.findViewById(R.id.sign_up_input_email)
        emailLayout = v.findViewById(R.id.sign_up_input_layout_email)

        usernameField = v.findViewById(R.id.sign_up_input_username)
        usernameLayout = v.findViewById(R.id.sign_up_input_layout_username)

        passwordField = v.findViewById(R.id.sign_up_input_password)
        passwordLayout = v.findViewById(R.id.sign_up_input_layout_password)

        confirmPasswordField = v.findViewById(R.id.sign_up_input_confirm_password)
        confirmPasswordLayout = v.findViewById(R.id.sign_up_input_layout_confirm_password)

        button_sign_up.setOnClickListener {
            signUp()
        }
    }

    private fun signUp() {
        doAsync {
            try {
                val params = JSONObject()

                val path = "/user/register"

                params.put("username", usernameField.text.toString())
                params.put("email", emailField.text.toString())
                params.put("password", passwordField.text.toString())
                params.put("confirm_password", confirmPasswordField.toString())

                apiController.post(Endpoint.LOCAL_API, path, params) { response ->
                    Log.d("RES", "$response")
                    validate(response)

                    // todo start map activity or whatevs
                }
            } catch (e: IOException) {
                Log.e("Login", "$e")
            }
        }

    }

    private fun validate(response: JSONObject?) {
        // todo do properly
    }
}