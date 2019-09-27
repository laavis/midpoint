package com.nopoint.midpoint

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nopoint.midpoint.fragments.FriendsFragment
import com.nopoint.midpoint.fragments.MapFragment
import com.nopoint.midpoint.fragments.SettingsFragment
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: ActionBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = supportActionBar!!
        bottom_navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        val fragment = MapFragment()
        loadFragment(fragment)
        toolbar.title = getString(R.string.nav_home)
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
        when (menuItem.itemId) {
            R.id.nav_home -> {
                val fragment = MapFragment()
                loadFragment(fragment)
                toolbar.title = getString(R.string.nav_home)
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_friends -> {
                val fragment = FriendsFragment()
                loadFragment(fragment)
                toolbar.title = getString(R.string.nav_friends)
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_settings -> {
                val fragment = SettingsFragment()
                loadFragment(fragment)
                toolbar.title = getString(R.string.nav_settings)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private fun loadFragment(fragment: Fragment) {
        // load fragment
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(container.id, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
