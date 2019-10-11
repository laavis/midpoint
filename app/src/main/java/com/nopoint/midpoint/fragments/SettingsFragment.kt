package com.nopoint.midpoint.fragments


import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import org.jetbrains.anko.sdk27.coroutines.textChangedListener


class SettingsFragment : Fragment() {
    private lateinit var currentUser: LocalUser
    private lateinit var sharedPref: SharedPref
    private lateinit var darkModeSwitch: SwitchMaterial

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        sharedPref = SharedPref(context!!)
        (activity as MainActivity).supportActionBar?.title = "Settings"
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        currentUser = CurrentUser.getLocalUser(activity as MainActivity)!!
        view.current_username_txt.text = currentUser.user.username
        val arrivedRadius = sharedPref.getArrivedRadius().toString()
        val placesRadius = sharedPref.getPlacesRadius()
        view.arrived_input.setText(arrivedRadius)
        view.place_radius_input.setText(placesRadius)
        view.place_radius_input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (!p0.isNullOrEmpty()) sharedPref.setPlacesRadius(p0.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
        view.arrived_input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (!p0.isNullOrEmpty()) sharedPref.setArrivedRadius(p0.toString().toDouble())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

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
        intent.putExtra("fragmentToLoad", "settings")
        startActivity(intent)
        (activity as MainActivity).finish()
    }
}
