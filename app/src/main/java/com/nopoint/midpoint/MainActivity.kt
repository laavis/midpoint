package com.nopoint.midpoint

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nopoint.midpoint.fragments.FriendsFragment
import com.nopoint.midpoint.fragments.MapFragment
import com.nopoint.midpoint.fragments.SettingsFragment
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import androidx.fragment.app.FragmentManager


class MainActivity : AppCompatActivity() {
    private val mapFragment = MapFragment()
    private val friendsFragment = FriendsFragment()
    private val settingsFragment = SettingsFragment()
    val fm: FragmentManager = supportFragmentManager
    var active: Fragment = mapFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        hasPermissions()
        bottom_navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        fm.beginTransaction().add(container.id, settingsFragment, "3").hide(settingsFragment).commit()
        fm.beginTransaction().add(container.id, friendsFragment, "2").hide(friendsFragment).commit()
        fm.beginTransaction().add(container.id, mapFragment, "1").commit()
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
        when (menuItem.itemId) {
            R.id.nav_home -> {
                fm.beginTransaction().hide(active).show(mapFragment).commit()
                active = mapFragment
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_friends -> {
                Log.d("FRIENDS", "FRIEND FRAGMENT FTW")
                fm.beginTransaction().hide(active).show(friendsFragment).commit()
                active = friendsFragment
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_settings -> {
                fm.beginTransaction().hide(active).show(settingsFragment).commit()
                active = settingsFragment
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }


    private fun hasPermissions() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }


}
