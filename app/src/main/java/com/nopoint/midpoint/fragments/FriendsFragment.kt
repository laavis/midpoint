package com.nopoint.midpoint.fragments


import android.content.Context
import android.content.Intent
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
import android.view.MenuItem
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.nopoint.midpoint.QRActivity
import com.nopoint.midpoint.adapters.FriendSearchAdapter
import com.nopoint.midpoint.adapters.FriendsListAdapter
import com.nopoint.midpoint.adapters.OnFriendListActionClickListener
import com.nopoint.midpoint.adapters.OnSendFriendReqBtnClickListener
import com.nopoint.midpoint.models.*
import com.nopoint.midpoint.networking.*
import kotlinx.android.synthetic.main.row_friend_search_results.*
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception
import kotlin.math.log

class FriendsFragment :
    Fragment(),
    OnSendFriendReqBtnClickListener,
    OnFriendListActionClickListener {

    private val service = ServiceVolley()
    private val apiController = APIController(service)

    private var isFriendRequestSuccess = false
    private var friendList = ArrayList<Friend>()
    private val sentFriendRequestsList = ArrayList<ReceivedFriendRequest>()
    private val receivedFriendRequestsList = ArrayList<ReceivedFriendRequest>()
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

            hideSearchViewKeyboard()

        }

        friends_open_qr.setOnClickListener {
            val intent = Intent(context!!.applicationContext, QRActivity::class.java)
            startActivity(intent)
            (activity as MainActivity).finish()
        }

        refreshLayout.setOnRefreshListener {
            getFriends()
        }

        getFriends()
    }

    private fun hideSearchViewKeyboard() {
        if (searchInput.requestFocus()) {
            val im = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(searchInput.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        token = CurrentUser.getLocalUser(activity!!)!!.token
    }

    private fun getFriends() {
        refreshLayout.isRefreshing = true
        friendList.clear()
        receivedFriendRequestsList.clear()
        apiController.get(FRIENDS_LIST, token) { res ->
            try {
                val friendsRes = Gson().fromJson(res.toString(), Friends::class.java) ?: throw Exception("Failed to connect")

                var friendCount = 0
                var sentReqCount = 0
                var receivedReqCount = 0

                // If user has friends (I don't)
                if (friendsRes.friends != null) {
                    friendsRes.friends.forEach {
                        friendList.add(Friend(it._id, it.username))
                    }

                    friendCount = friendsRes.friends.size
                }

                if (friendsRes.received_requests != null) {
                    friendsRes.received_requests.forEach {
                        receivedFriendRequestsList.add(ReceivedFriendRequest(
                            it._id,
                            it.requester,
                            it.receiver,
                            it.status,
                            it.req_username
                        ))
                    }

                    receivedReqCount = friendsRes.received_requests.size
                }

                if (friendsRes.sent_requests != null) {
                    friendsRes.sent_requests.forEach {
                        sentFriendRequestsList.add(ReceivedFriendRequest(
                            it._id,
                            it.requester,
                            it.receiver,
                            it.status,
                            it.rec_username
                        ))
                    }

                    sentReqCount = friendsRes.sent_requests.size
                }

                if (sentReqCount == 0 || receivedReqCount == 0) {
                    val dp = 8
                    val scale = resources.displayMetrics.density
                    val padding = (dp * scale + 0.5f).toInt()
                    friends_list.setPadding(0, padding, 0, 0)
                }

                refreshLayout.isRefreshing = false

                val friendListAnimation = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation)

                friends_list.layoutAnimation = friendListAnimation

                val rows = calculateRowCount(
                    friendCount,
                    receivedReqCount,
                    sentReqCount)

                initFLRecyclerView(rows)

            } catch (e: Exception) {
                refreshLayout.isRefreshing = false
                Log.e("FRIENDS", "getFriends: $e")
            }
        }
    }

    // Calculate total row count for recycler view
    private fun calculateRowCount(numFriends: Int, numReceivedFriendRequests: Int, numSentFriendRequests: Int): ArrayList<FriendsListAdapter.IRowFriend> {
        val rows = ArrayList<FriendsListAdapter.IRowFriend>()

        for (i in 0 until numReceivedFriendRequests) {
            rows.add(FriendsListAdapter.ReceivedFriendRequestRow(receivedFriendRequestsList[i].req_username))
        }

        for (i in 0 until numSentFriendRequests) {
            Log.d("FRIENDS", "${sentFriendRequestsList[i].req_username}")
            rows.add(FriendsListAdapter.SentFriendRequestRow(sentFriendRequestsList[i].req_username))
        }

        for (i in 0 until numFriends) {
            rows.add(FriendsListAdapter.FriendRow(friendList[i].username))
        }

        return rows
    }

    data class DeleteFriend(
        @SerializedName("error")val error: String?
    )

    private fun deleteFriend(position: Int) {
        val body = JSONObject()
        val friendToBeDeleted = friendList[position].username

        body.put("friend_username", friendToBeDeleted)

        apiController.post(DELETE_FRIEND, body, token) { res ->
            try {
                val deleteRes = Gson().fromJson(res.toString(), DeleteFriend::class.java)

                if (deleteRes.error != null) {
                    throw Exception("${deleteRes.error}")
                }

                getFriends()

            } catch (e: Exception) {
                Toast.makeText(context, "${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("FRIENDS", "$e")
            }
        }
    }



    private fun respondToFriendRequest(position: Int, status: Int, msg: String) {
        val body = JSONObject()
        receivedFriendRequestsList[position]._id
        body.put("status", status)
        body.put("request_id", receivedFriendRequestsList[position]._id)

        apiController.post(FRIENDS_RESPOND, body, token) { res ->
            try {
                val respond = Gson().fromJson(res.toString(), FriendRequestRespond::class.java)

                if (respond.success) {
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
        val msg = "You and ${receivedFriendRequestsList[position].req_username} are now friends!"
        respondToFriendRequest(position, 1, msg)
    }

    override fun onDenyClicked(button: MaterialButton, position: Int) {
        val msg = "You denied friend request from ${receivedFriendRequestsList[position].req_username}"
        respondToFriendRequest(position, 2, msg)
    }

    override fun onDeleteClicked(menuItem: MenuItem, position: Int) {
        deleteFriend(position)
    }

    // Send friend request
    override fun onButtonClicked(button: ImageButton, statusIcon: ImageView, position: Int) {
        val body = JSONObject()

        body.put("receiver", searchResults[position].username)

        apiController.post(FRIEND_SEND_REQUEST_URL, body, token) { res ->
            try {
                val parsedRes = Gson().fromJson(res.toString(), SentFriendReqRes::class.java)

                if (parsedRes.success) {
                    friends_button_send_friend_request.visibility = View.GONE
                    friends_req_status.visibility = View.VISIBLE
                    isFriendRequestSuccess = true
                    button.visibility = View.GONE
                    statusIcon.visibility = View.VISIBLE
                }

                hideSearchViewKeyboard()
                friends_fab_add.isExpanded = false

            } catch (e: IOException) {
                Log.e("FRIENDS | SEND REQUEST", "$e")
            }
        }
    }

    private fun getSearchResults() {
        if (searchInput.text.isNullOrEmpty()) return

        val path = "/search/users?username=${searchInput.text}"

        apiController.get(path, token) { response ->
            try {
                searchResults.clear()
                val searchResponse = Gson().fromJson(response.toString(), UserSearchResponse::class.java)

                if (searchResponse.users.isNotEmpty()) {
                    searchResponse.users.forEach {
                        searchResults.add(UserSearchResponseUser(it._id, it.username, it.isFriend, it.isRequestSent))
                    }

                } else {
                    searchResults.clear()
                }
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
