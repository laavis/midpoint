package com.nopoint.midpoint.fragments


import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nopoint.midpoint.MainActivity
import com.nopoint.midpoint.R
import kotlinx.android.synthetic.main.fragment_friends.*
import android.view.WindowManager
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nopoint.midpoint.models.CurrentUser
import com.nopoint.midpoint.models.LocalUser
import org.json.JSONObject
import java.util.TimerTask
import java.util.Timer
import android.text.Editable
import android.util.Log
import com.google.gson.Gson
import com.nopoint.midpoint.models.UserSearchResponse
import com.nopoint.midpoint.networking.API
import com.nopoint.midpoint.networking.APIController
import com.nopoint.midpoint.networking.ServiceVolley
import java.io.IOException


class FriendsFragment : Fragment() {

    private val service = ServiceVolley()
    private val apiController = APIController(service)

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var localUser: LocalUser
    private lateinit var searchInput: EditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_friends, container, false)

        searchInput = view.findViewById(R.id.search_input)
        bottomNav = (activity as MainActivity).findViewById<BottomNavigationView>(R.id.bottom_navigation)

        (activity as MainActivity).supportActionBar?.title = "Friends"

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var timer = Timer()


        val searchTextWatcher = object : TextWatcher {
            override fun afterTextChanged(arg0: Editable) {
                // user typed: start the timer
                timer = Timer()
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        // do your actual work here
                        Log.d("AF", "times up")
                        getSearchResults()
                    }
                }, 2000) // 600ms delay before the timer executes the „run“ method from TimerTask
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // nothing to do here
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // user is typing: reset already started timer (if existing)
                timer?.cancel()
            }
        }

        searchInput.addTextChangedListener(searchTextWatcher)


        friends_fab_add.setOnClickListener {
            friends_fab_add.isExpanded = true

        }

        friends_add_return.setOnClickListener {
            friends_fab_add.isExpanded = false
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        localUser = CurrentUser.getLocalUser(activity!!)!!
    }



    private fun getSearchResults() {
        val params = JSONObject()
        val path = "/search/users"

        params.put("username", searchInput.text)
        apiController.post(API.LOCAL_API, path, params, localUser.token) { response ->
            try {
                val searchResponse = Gson().fromJson(response.toString(), UserSearchResponse::class.java)
                Log.d("FRIENDS|ADD|SEARCH", "$searchResponse")



            } catch (e: IOException) {
                Log.e("FRIENDS|ADD|SEARCH", "$e")
            }
        }
    }

    private fun setStatusBarColor(colorId: Int) {
            val ma = activity as MainActivity
            val window = ma.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(ma, colorId)
    }
}
