package com.nopoint.midpoint

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nopoint.midpoint.fragments.FriendsFragment
import com.nopoint.midpoint.fragments.MapFragment
import com.nopoint.midpoint.fragments.SettingsFragment
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottom_navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        val fragment = MapFragment()
        loadFragment(fragment)
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
        when (menuItem.itemId) {
            R.id.nav_home -> {
                val fragment = MapFragment()
                loadFragment(fragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_friends -> {
                val fragment = FriendsFragment()
                loadFragment(fragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_settings -> {
                val fragment = SettingsFragment()
                loadFragment(fragment)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private fun loadFragment(fragment: Fragment) {
        // load fragment
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(container.id, fragment)
        //transaction.addToBackStack(null)
        transaction.commit()
    }
}
