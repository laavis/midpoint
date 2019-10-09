package com.nopoint.midpoint.fragments

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.nopoint.midpoint.EntryActivity
import com.nopoint.midpoint.R
import com.nopoint.midpoint.models.SignUpErrorResponse
import com.nopoint.midpoint.models.SignUpResponse
import com.nopoint.midpoint.networking.APIController
import com.nopoint.midpoint.networking.REGISTER
import com.nopoint.midpoint.networking.ServiceVolley
import kotlinx.android.synthetic.main.fragment_sign_up.*
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

        setSpacing()

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

    private fun setSpacing() {
        val displayMetrics = DisplayMetrics()
        (activity as EntryActivity).windowManager.defaultDisplay.getMetrics(displayMetrics)

        val height = displayMetrics.heightPixels
        val containerPaddingTop = if (height < 2400) (height * 0.1).toInt() else (height * 0.15).toInt()

        sign_up_container.setPadding(0, containerPaddingTop, 0, 0)
    }

    private fun signUp() {
        val body = JSONObject()

        body.put("username", usernameField.text.toString())
        body.put("email", emailField.text.toString())
        body.put("password", passwordField.text.toString())
        body.put("confirm_password", confirmPasswordField.text.toString())


        apiController.post(REGISTER, body) { response ->
            try {
                val signUpRes = Gson().fromJson(response.toString(), SignUpResponse::class.java)

                if (signUpRes.success) {
                    (activity as EntryActivity).getLoginFragment()
                } else {
                    val signUpErrorResponse = Gson().fromJson(response.toString(), SignUpErrorResponse::class.java)
                    setErrors(signUpErrorResponse)
                }

            } catch (e: IOException) {
                Log.e("SIGN UP", "$e")
            }
        }
    }

    private fun setErrors(signUpErrRes: SignUpErrorResponse) {
        emailLayout.error = signUpErrRes.email
        usernameLayout.error = signUpErrRes.username
        passwordLayout.error = signUpErrRes.password
        confirmPasswordLayout.error = signUpErrRes.confirm_password
    }
}


