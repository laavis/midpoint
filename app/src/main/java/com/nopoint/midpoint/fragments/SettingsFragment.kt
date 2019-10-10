package com.nopoint.midpoint.fragments


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.switchmaterial.SwitchMaterial
import com.nopoint.midpoint.EntryActivity
import com.nopoint.midpoint.MainActivity
import com.nopoint.midpoint.R
import com.nopoint.midpoint.SharedPref
import com.nopoint.midpoint.models.CurrentUser
import com.nopoint.midpoint.models.LocalUser
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.fragment_settings.view.*
import org.jetbrains.anko.support.v4.act


class SettingsFragment : Fragment() {
    private lateinit var currentUser: LocalUser

    private lateinit var sharedPref: SharedPref

    private lateinit var darkModeSwitch: SwitchMaterial
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        sharedPref = SharedPref(context!!)

        (activity as MainActivity).supportActionBar?.title = "Settings"
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        currentUser = CurrentUser.getLocalUser(activity as MainActivity)!!
        view.current_email_txt.text = "Email: ${currentUser.user.email}"
        view.current_username_txt.text = "Username: ${currentUser.user.username}"
        view.logout_btn.setOnClickListener {
            CurrentUser.clearUser(activity as MainActivity)
            val intent = Intent(context!!.applicationContext, EntryActivity::class.java)
            startActivity(intent)
            (activity as MainActivity).finish()

        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        darkModeSwitch = view.findViewById(R.id.dark_mode_switch)


        if (sharedPref.loadNightModeState() == true) {
           dark_mode_switch.isChecked = true
       }

       darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
           if (isChecked) {
               // The switch is enabled/checked
               sharedPref.setNightModeState(true)
               AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
               applyThemeChanges()

           } else {
               // The switch is disabled
               sharedPref.setNightModeState(false)
               AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
               applyThemeChanges()

           }
       }

    }

    private fun applyThemeChanges() {
        val intent = Intent(context!!.applicationContext, MainActivity::class.java)
        startActivity(intent)
        (activity as MainActivity).finish()
    }

}
