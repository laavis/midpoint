package com.nopoint.midpoint.fragments


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nopoint.midpoint.EntryActivity
import com.nopoint.midpoint.MainActivity
import com.nopoint.midpoint.R
import com.nopoint.midpoint.models.CurrentUser
import kotlinx.android.synthetic.main.fragment_settings.view.*

/**
 * A simple [Fragment] subclass.
 */
class SettingsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        (activity as MainActivity).supportActionBar?.title = "Settings"
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        view.logout_btn.setOnClickListener {
            CurrentUser.clearUser(activity as MainActivity)
            val intent = Intent(context!!.applicationContext, EntryActivity::class.java)
            startActivity(intent)
            (activity as MainActivity).finish()
        }
        return view
    }



}
