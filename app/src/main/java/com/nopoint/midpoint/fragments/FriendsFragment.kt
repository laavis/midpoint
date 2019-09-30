package com.nopoint.midpoint.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nopoint.midpoint.MainActivity

import com.nopoint.midpoint.R
import kotlinx.android.synthetic.main.fragment_friends.*

/**
 * A simple [Fragment] subclass.
 */
class FriendsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val mainActivity = MainActivity()
        (activity as MainActivity).supportActionBar?.title = "Friends"

        return inflater.inflate(R.layout.fragment_friends, container, false)
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


}
