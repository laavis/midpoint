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
import java.util.TimerTask
import java.util.Timer
import android.text.Editable
import android.util.Log
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.nopoint.midpoint.adapters.FriendSearchAdapter
import com.nopoint.midpoint.adapters.FriendsListAdapter
import com.nopoint.midpoint.adapters.OnRespondFriendRequestClickListener
import com.nopoint.midpoint.adapters.OnSendFriendReqBtnClickListener
import com.nopoint.midpoint.models.*
import com.nopoint.midpoint.networking.*
import kotlinx.android.synthetic.main.row_friend_search_results.*
import org.json.JSONObject
import java.io.IOException

class FriendsFragment :
    Fragment(),
    OnSendFriendReqBtnClickListener,
    OnRespondFriendRequestClickListener {

    private val service = ServiceVolley()
    private val apiController = APIController(service)

    private var isFriendRequestSuccess = false
    private var friendList = ArrayList<Friend>()
    private val friendRequestsList = ArrayList<FriendRequest>()
    private var searchResults = ArrayList<UserSearchResponseUser>()


    private lateinit var token: String
    private lateinit var searchInput: EditText
    private lateinit var searchAdapter: FriendSearchAdapter
    private lateinit var friendListAdapter: FriendsListAdapter

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

        refreshLayout.setOnRefreshListener {
            getFriends()
        }

        getFriends()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        token = CurrentUser.getLocalUser(activity!!)!!.token
    }

    private fun getFriends() {
        refreshLayout.isRefreshing = true
        friendList.clear()
        friendRequestsList.clear()
        apiController.get(API.LOCAL_API, FRIENDS_LIST, token) {res ->
            try {
                val friendsRes = Gson().fromJson(res.toString(), Friends::class.java)

                // If user has friends
                if(friendsRes.friends != null) {
                    friendsRes.friends.forEach {
                        friendList.add(Friend(it._id, it.username))
                    }
                }

                if(friendsRes.requests != null) {
                    friendsRes.requests.forEach {
                        friendRequestsList.add(FriendRequest(
                            it._id,
                            it.requester,
                            it.receiver,
                            it.status,
                            it.req_username
                        ))
                    }
                }
                refreshLayout.isRefreshing = false

                val rows = calculateRowCount(friendsRes.friends!!.size, friendsRes.requests!!.size)

                initFLRecyclerView(rows)

            } catch (e: IOException) {
                refreshLayout.isRefreshing = false
                Log.e("FRIENDS", "$e")
            }
        }
    }


    private fun calculateRowCount(numFriends: Int, numFriendRequests: Int): ArrayList<FriendsListAdapter.IRowFriend> {
        val rows = ArrayList<FriendsListAdapter.IRowFriend>()

        for (i in 0 until numFriendRequests) {
            rows.add(FriendsListAdapter.FriendRequestRow(friendRequestsList[i].req_username))
        }

        for (i in 0 until numFriends) {
            rows.add(FriendsListAdapter.FriendRow(friendList[i].username))
        }
        return rows
    }


    private fun respondToFriendRequest(position: Int, status: Int, msg: String) {
        val body = JSONObject()
        friendRequestsList[position]._id
        body.put("status", status)
        body.put("request_id", friendRequestsList[position]._id)

        apiController.post(API.LOCAL_API, FRIENDS_RESPOND, body, token) { res ->
            try {
                val respond = Gson().fromJson(res.toString(), FriendRequestRespond::class.java)

                if (respond.success) {
                    val snackbar = Snackbar.make(activity!!.findViewById(R.id.content), msg, Snackbar.LENGTH_LONG).show()
                    getFriends()
                } else {
                    val snackbar = Snackbar.make(activity!!.findViewById(R.id.content), "Something went wrong", Snackbar.LENGTH_LONG)
                    snackbar.setBackgroundTint(resources.getColor(R.color.orange))
                    snackbar.show()
                }

            } catch (e: IOException) {
                Log.e("FRIENDS | SEND RESPOND", "$e")
            }
        }
    }

    override fun onAcceptClicked(button: MaterialButton, position: Int) {
        val msg = "You and ${friendRequestsList[position].req_username} are now friends!"
        respondToFriendRequest(position, 1, msg)
    }

    override fun onDenyClicked(button: MaterialButton, position: Int) {
        val msg = "You denied friend request from ${friendRequestsList[position].req_username}"
        respondToFriendRequest(position, 2, msg)
    }

    // Send friend request
    override fun onItemClicked(button: ImageButton, statusIcon: ImageView, position: Int) {
        val body = JSONObject()

        body.put("receiver", searchResults[position].username)

        apiController.post(API.LOCAL_API, FRIEND_SEND_REQUEST_URL, body, token) { res ->
            try {
                val parsedRes = Gson().fromJson(res.toString(), SentFriendReqRes::class.java)

                if (parsedRes.success) {
                    friends_button_send_friend_request.visibility = View.GONE
                    friends_req_status.visibility = View.VISIBLE
                    isFriendRequestSuccess = true
                    button.visibility = View.GONE
                    statusIcon.visibility = View.VISIBLE
                }

            } catch (e: IOException) {
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
                initFSRecyclerView(searchResults)

            } catch (e: IOException) {
                Log.e("FRIENDS|ADD|SEARCH", "$e")
            }
        }
    }

    private fun initFLRecyclerView(rows: ArrayList<FriendsListAdapter.IRowFriend>) {
        friendListAdapter = FriendsListAdapter(this, rows, (activity as MainActivity))
        friends_list.layoutManager = LinearLayoutManager((activity as MainActivity))
        friends_list.adapter = friendListAdapter
    }

    private fun initFSRecyclerView(list: ArrayList<UserSearchResponseUser>) {
        searchAdapter = FriendSearchAdapter(
            list,
            (activity as MainActivity),
            this
        )
        friends_search_list.layoutManager = LinearLayoutManager((activity as MainActivity))
        friends_search_list.adapter = searchAdapter
    }

}
