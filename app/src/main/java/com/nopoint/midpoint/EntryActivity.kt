package com.nopoint.midpoint

import android.content.pm.PackageManager
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.RelativeLayout
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import kotlinx.android.synthetic.main.activity_entry.*
import android.widget.TextView
import com.nopoint.midpoint.fragments.LoginFragment
import com.nopoint.midpoint.fragments.SignUpFragment


class EntryActivity : AppCompatActivity() {


    private lateinit var fTransaction: FragmentTransaction
    private lateinit var fManager: FragmentManager
    private lateinit var loginFragment: LoginFragment
    private lateinit var signUpFragment: SignUpFragment

    private  var isLogin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry)
        hasPermissions()
        /*start_map_btn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            //Sets the map activity as the new root
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        // network_btn.setOnClickListener {sendPostRequest()} */

        val strokeParams = entry_stroke.layoutParams as RelativeLayout.LayoutParams

        fManager = supportFragmentManager
        fTransaction = fManager.beginTransaction()

        loginFragment = LoginFragment()
        signUpFragment = SignUpFragment()

        getLoginFragment()
        isLogin = true
        toggleTextStyle(textbutton_show_login, textbutton_show_sign_up)

        textbutton_show_login.setOnClickListener {
            isLogin = true

            getLoginFragment()

            toggleTextStyle(textbutton_show_login, textbutton_show_sign_up)
            strokeParams.addRule(RelativeLayout.ALIGN_START, R.id.textbutton_show_login)
            strokeParams.addRule(RelativeLayout.ALIGN_END, R.id.textbutton_show_login)
        }

        textbutton_show_sign_up.setOnClickListener {
            isLogin = false

            getSignUpFragment()

            toggleTextStyle(textbutton_show_sign_up, textbutton_show_login)
            strokeParams.addRule(RelativeLayout.ALIGN_START, R.id.textbutton_show_sign_up)
            strokeParams.addRule(RelativeLayout.ALIGN_END, R.id.textbutton_show_sign_up)
            entry_stroke.layoutParams = strokeParams
        }
    }

    private fun toggleTextStyle(active: TextView, inactive: TextView) {
        active.typeface = Typeface.DEFAULT_BOLD
        inactive.typeface = Typeface.DEFAULT
    }

    private fun getLoginFragment() {
        fTransaction = fManager.beginTransaction()
        fTransaction.replace(R.id.entry_fragment_container, loginFragment)
        fTransaction.commit()
    }

    private fun getSignUpFragment() {
        fTransaction = fManager.beginTransaction()
        fTransaction.replace(R.id.entry_fragment_container, signUpFragment)
        fTransaction.commit()
    }

    private fun hasPermissions() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }


}
