package com.nopoint.midpoint.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nopoint.midpoint.MainActivity
import com.nopoint.midpoint.R

/**
 * A simple [Fragment] subclass.
 */
class SettingsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        (activity as MainActivity).supportActionBar?.title = "Settings"

        return inflater.inflate(R.layout.fragment_settings, container, false)
    }


}
