package com.nopoint.midpoint.fragments


import android.content.Context
import android.os.Bundle
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nopoint.midpoint.MainActivity
import com.nopoint.midpoint.R
import kotlinx.android.synthetic.main.fragment_friends.*
import android.widget.EditText
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.TimerTask
import java.util.Timer
import android.text.Editable
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.nopoint.midpoint.adapters.FriendSearchAdapter
import com.nopoint.midpoint.adapters.OnSendFriendReqBtnClickListener
import com.nopoint.midpoint.models.*
import com.nopoint.midpoint.networking.API
import com.nopoint.midpoint.networking.APIController
import com.nopoint.midpoint.networking.FRIEND_SEND_REQUEST_URL
import com.nopoint.midpoint.networking.ServiceVolley
import kotlinx.android.synthetic.main.row_friend_search_results.*
import org.json.JSONObject
import java.io.IOException

class FriendsFragment : Fragment(), OnSendFriendReqBtnClickListener {

    private val service = ServiceVolley()

    private val apiController = APIController(service)

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var token: String
    private lateinit var searchInput: EditText

    private lateinit var adapter: FriendSearchAdapter

    private var isFriendRequestSuccess = false

    private var searchResults = ArrayList<UserSearchResponseUser>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_friends, container, false)
        searchInput = v.findViewById(R.id.search_input)
        return v
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
                        getSearchResults()
                    }
                }, 2000) // wait 2s before executing run()
            }

            // nothing to do here
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // user typing reset timer if existing
                timer.cancel()
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
        token = CurrentUser.getLocalUser(activity!!)!!.token
    }

    private fun initRecyclerView(list: ArrayList<UserSearchResponseUser>) {
        adapter = FriendSearchAdapter(
            list,
            (activity as MainActivity),
            this
        )
        friends_search_list.layoutManager = LinearLayoutManager((activity as MainActivity))
        friends_search_list.adapter = adapter
    }

    override fun onItemClicked(button: ImageButton, statusIcon: ImageView, position: Int) {
        // sendFriendRequest(searchResults[position].username)

        val body = JSONObject()

        body.put("receiver", searchResults[position].username)

        apiController.post(API.LOCAL_API, FRIEND_SEND_REQUEST_URL, body, token) { res ->
            try {
                val parsedRes = Gson().fromJson(res.toString(), FriendRequestResponse::class.java)
                Log.d("FRIENDS | res", "$parsedRes")

                if (parsedRes.success) {
                    friends_button_send_friend_request.visibility = View.GONE
                    friends_req_status.visibility = View.VISIBLE
                    isFriendRequestSuccess = true
                    button.visibility = View.GONE
                    statusIcon.visibility = View.VISIBLE
                }

            }catch (e: IOException) {
                Log.e("FRIENDS | SEND REQUEST", "$e")
            }
        }
    }

    private fun sendFriendRequest(username: String) {
        val body = JSONObject()

        body.put("receiver", username)

        apiController.post(API.LOCAL_API, FRIEND_SEND_REQUEST_URL, body, token) { res ->
            try {
                val parsedRes = Gson().fromJson(res.toString(), FriendRequestResponse::class.java)
                Log.d("FRIENDS | res", "$parsedRes")

                if (parsedRes.success) {
                    friends_button_send_friend_request.visibility = View.GONE
                    friends_req_status.visibility = View.VISIBLE
                    isFriendRequestSuccess = true
                }

            }catch (e: IOException) {
                Log.e("FRIENDS | SEND REQUEST", "$e")
            }
        }
    }

    private fun getSearchResults() {
        if (searchInput.text.isNullOrEmpty()) return

        val path = "/search/users?username=${searchInput.text}"


        apiController.get(API.LOCAL_API, path, token) { response ->
            try {
                Log.d("FRIENDS | ORIGINAL RESPONSE", response.toString())
                searchResults.clear()
                val searchResponse = Gson().fromJson(response.toString(), UserSearchResponse::class.java)

                if (searchResponse.users.isNotEmpty()) {
                    searchResponse.users.forEach {
                        searchResults.add(UserSearchResponseUser(it._id, it.username, it.isFriend, it.isRequestSent))
                    }

                } else {
                    searchResults.clear()
                }
                Log.d("FRIEND", "list: $searchResults")
                initRecyclerView(searchResults)

            } catch (e: IOException) {
                Log.e("FRIENDS|ADD|SEARCH", "$e")
            }
        }
    }

}
