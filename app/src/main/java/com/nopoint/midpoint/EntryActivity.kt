package com.nopoint.midpoint

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.widget.RelativeLayout
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import kotlinx.android.synthetic.main.activity_entry.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import com.nopoint.midpoint.fragments.LoginFragment
import com.nopoint.midpoint.fragments.SignUpFragment
import com.nopoint.midpoint.models.CurrentUser


class EntryActivity : AppCompatActivity() {

    private lateinit var fTransaction: FragmentTransaction
    private lateinit var fManager: FragmentManager
    private lateinit var loginFragment: LoginFragment
    private lateinit var signUpFragment: SignUpFragment

    private  var isLogin = false

    private lateinit var sharedpref: SharedPref

    override fun onCreate(savedInstanceState: Bundle?) {

        sharedpref = SharedPref(this)

        if (sharedpref.loadNightModeState() == true) {
            setTheme(R.style.DarkTheme)
        } else {
            setTheme(R.style.LightTheme)
        }

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_entry)
        val strokeParams = entry_stroke.layoutParams as RelativeLayout.LayoutParams
        if(checkValidSession()){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }


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



    private fun checkValidSession(): Boolean {
        return (CurrentUser.getLocalUser(this) != null)
    }

    private fun toggleTextStyle(active: TextView, inactive: TextView) {
        active.typeface = Typeface.DEFAULT_BOLD
        inactive.typeface = Typeface.DEFAULT
    }

    fun getLoginFragment() {
        fTransaction = fManager
            .beginTransaction()
            .setCustomAnimations(
                R.anim.enter_left_to_right,
                R.anim.exit_left_to_right,
                R.anim.enter_right_to_left,
                R.anim.exit_right_to_left
            )
            .addToBackStack(null)
        fTransaction.replace(R.id.entry_fragment_container, loginFragment)
        fTransaction.commit()
    }

    private fun getSignUpFragment() {
        fTransaction = fManager
            .beginTransaction()
            .setCustomAnimations(
                R.anim.enter_right_to_left,
                R.anim.exit_right_to_left,
                R.anim.enter_left_to_right,
                R.anim.exit_left_to_right
            )
            .addToBackStack(null)
        fTransaction.replace(R.id.entry_fragment_container, signUpFragment)
        fTransaction.commit()
    }

}
