package com.nopoint.midpoint

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nopoint.midpoint.fragments.FriendsFragment
import com.nopoint.midpoint.fragments.MapFragment
import com.nopoint.midpoint.fragments.SettingsFragment
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import androidx.fragment.app.FragmentManager
import com.nopoint.midpoint.models.Friend
import org.jetbrains.anko.doAsync


class MainActivity : AppCompatActivity() {
    private val mapFragment = MapFragment()
    private val friendsFragment = FriendsFragment()
    private val settingsFragment = SettingsFragment()
    private val fm: FragmentManager = supportFragmentManager
    private var active: Fragment = mapFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPref = SharedPref(this)

        if (sharedPref.loadNightModeState() == true) {
            setTheme(R.style.DarkTheme)
        } else {
            setTheme(R.style.LightTheme)
        }
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)


        if (hasPermissions()) {
            initFragments()
        }

        val intentFragment = intent.extras?.getString("fragmentToLoad")
        getFragmentFromIntent(intentFragment)
    }

    private fun getFragmentFromIntent(intentFragment: String?) {

        when (intentFragment) {
                "friend" -> {
                    Log.d("MAIN", "$intentFragment")
                    getFriendsFragment()
                }
                "settings" -> getSettingsFragment()
                else -> getMapFragment()
        }
    }

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    getMapFragment()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.nav_friends -> {
                    getFriendsFragment()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.nav_settings -> {
                    getSettingsFragment()
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    private fun getSettingsFragment() {
        fm.beginTransaction().hide(active).show(settingsFragment).commit()
        active = settingsFragment
    }

    private fun getMapFragment() {
        fm.beginTransaction().hide(active).show(mapFragment).commit()
        active = mapFragment
    }

    private fun getFriendsFragment() {
        fm.beginTransaction().hide(active).show(friendsFragment).commit()
        active = friendsFragment
    }

    private fun initFragments() {
        bottom_navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        fm.beginTransaction().add(container.id, settingsFragment, "3").hide(settingsFragment)
            .commit()
        fm.beginTransaction().add(container.id, friendsFragment, "2").hide(friendsFragment)
            .commit()
        fm.beginTransaction().add(container.id, mapFragment, "1").commit()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    initFragments()
                }
                return
            }
        }
    }

    private fun hasPermissions(): Boolean {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            return true
        }
        return false
    }
}
