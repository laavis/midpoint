package com.nopoint.midpoint.fragments


import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nopoint.midpoint.MainActivity
import com.nopoint.midpoint.R
import kotlinx.android.synthetic.main.fragment_friends.*
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView


/**
 * A simple [Fragment] subclass.
 */
class FriendsFragment : Fragment() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_friends, container, false)
        bottomNav = (activity as MainActivity).findViewById<BottomNavigationView>(R.id.bottom_navigation)

        (activity as MainActivity).supportActionBar?.title = "Friends"

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        friends_fab_add.setOnClickListener {
            friends_fab_add.isExpanded = true

        }

        friends_add_return.setOnClickListener {
            friends_fab_add.isExpanded = false
        }
    }

    private fun setStatusBarColor(colorId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val ma = activity as MainActivity
            val window = ma.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(ma, colorId)
        }
    }

}
