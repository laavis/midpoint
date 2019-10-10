package com.nopoint.midpoint.fragments

import android.app.AlertDialog
import android.content.*
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.model.LatLng
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.nopoint.midpoint.*
import com.nopoint.midpoint.adapters.MeetingRequestViewListener
import com.nopoint.midpoint.adapters.MeetingRequestsAdapter
import com.nopoint.midpoint.map.DirectionsUtils
import com.nopoint.midpoint.map.MeetingUtils
import com.nopoint.midpoint.map.MeetingsSingleton
import com.nopoint.midpoint.map.PlacesUtils
import com.nopoint.midpoint.map.models.Direction
import com.nopoint.midpoint.models.*
import com.nopoint.midpoint.networking.API
import com.nopoint.midpoint.networking.APIController
import com.nopoint.midpoint.networking.ServiceVolley
import com.nopoint.midpoint.networking.*
import kotlinx.android.synthetic.main.fragment_meeting.view.*
import kotlinx.android.synthetic.main.request_dialog.*
import kotlinx.android.synthetic.main.request_dialog.view.*
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception

/**
 * Fragment for meeting requests
 */
class MeetingFragment : Fragment(), MeetingRequestViewListener {
    private val service = ServiceVolley()
    private val apiController = APIController(service)
    var currentLocation: LatLng? = null
    private lateinit var localUser: LocalUser
    private lateinit var sharedPref: SharedPref

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_meeting, container, false)
        sharedPref = SharedPref(context!!)
        // view.request_btn.setOnClickListener { sendRequest(view.friend_username.text.toString()) }
        view.refresh_btn.setOnClickListener { getRequests() }
        view.new_request_btn.setOnClickListener { createDialog() }
        LocalBroadcastManager.getInstance(context!!.applicationContext)
            .registerReceiver(mLocalBroadcastReceiver, getLocalIntentFilter())
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        localUser = CurrentUser.getLocalUser(activity!!)!!
        getRequests()
    }

    // RESPOND TO MEETING REQUEST
    private fun sendResponse(meetingRequest: MeetingRequest) {
        val latestLocation = currentLocation
        val fullRouteURL = DirectionsUtils.buildUrlFromLatLng(
            LatLng(latestLocation!!.latitude, latestLocation.longitude),
            LatLng(meetingRequest.requesterLatitude, meetingRequest.requesterLongitude)
        )
        Log.d("MEETING", fullRouteURL)
        // Get full route
        apiController.get(API.DIRECTIONS, fullRouteURL) { response ->
            if (response != null) {
                val result = Gson().fromJson(response.toString(), Direction::class.java)
                Log.d("MEETING", "$result")
                val midpointLatLng: LatLng? = DirectionsUtils.getAbsoluteMidpoint(result)
                // Midpoint calculation success
                if (midpointLatLng != null) {
                    val body = JSONObject()
                    body.put("requestId", meetingRequest.id)
                    body.put("lat", latestLocation.latitude)
                    body.put("lng", latestLocation.longitude)
                    body.put("response", 1) //TODO allow setting different response types
                    // Hardcoded for meeting at a cafe
                    if (meetingRequest.status == 3) {
                        val placesUrl = PlacesUtils.buildUrl(midpointLatLng, sharedPref.getPlacesRadius())
                        apiController.get(API.PLACES, placesUrl) { placesResponse ->
                            Log.d("PLACES", placesResponse.toString())
                            val places =
                                Gson().fromJson(placesResponse.toString(), Places::class.java)
                            if (places.results.isNotEmpty()) {
                                val best = places.results[0]
                                body.put("middleLat", best.geometry.location.lat)
                                body.put("middleLng", best.geometry.location.lng)
                                body.put("middlePointName", best.name)
                            } else {
                                body.put("middleLat", midpointLatLng.latitude)
                                body.put("middleLng", midpointLatLng.longitude)
                            }
                            sendMeetingRequestResponse(body)
                        }
                    } else {
                        body.put("middleLat", midpointLatLng.latitude)
                        body.put("middleLng", midpointLatLng.longitude)
                        sendMeetingRequestResponse(body)
                    }
                }
            }
        }
    }

    private fun sendMeetingRequestResponse(body: JSONObject) {
        // Respond to requester
        apiController.post(MEETING_RESPOND_URL, body, localUser.token) {
            try {
                if (!it?.optString("msg").isNullOrEmpty()) {
                    val newMeeting = Gson().fromJson(it.toString(), MidpointResponse::class.java)
                    val midpointURL = DirectionsUtils.buildUrlFromLatLng(
                        LatLng(currentLocation!!.latitude, currentLocation!!.longitude),
                        LatLng(newMeeting.meetingPointLat, newMeeting.meetingPointLng)
                    )
                    val map = parentFragment as MapFragment
                    map.getDirectionsToAbsoluteMidpoint(
                        midpointURL,
                        true,
                        newMeeting.middlePointName
                    )
                    getRequests()
                }
            } catch (e: IOException) {
                Log.e("MEETING", "$e")
            }
        }
    }

    override fun acceptRequest(meetingRequest: MeetingRequest) {
        val active = MeetingsSingleton.getActiveMeeting()
        if (active != null) {
            MaterialAlertDialogBuilder(activity)
                .setTitle("Warning")
                .setMessage("You already have an active meeting, want to delete it?")
                .setPositiveButton("Delete") { _, _ ->
                    deleteRequest(active)
                    sendResponse(meetingRequest)
                }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            sendResponse(meetingRequest)
        }
    }

    override fun showOnMap(meetingRequest: MeetingRequest) {
        val midpointURL = DirectionsUtils.buildUrlFromLatLng(
            LatLng(currentLocation!!.latitude, currentLocation!!.longitude),
            LatLng(meetingRequest.meetingPointLatitude!!, meetingRequest.meetingPointLongitude!!)
        )
        val map = parentFragment as MapFragment
        map.getDirectionsToAbsoluteMidpoint(
            midpointURL,
            clearPrevious = true,
            meetingPointName = meetingRequest.meetingPointName
        )
/*        if (localUser.user.id == meetingRequest.receiver) {
            val otherPersonUrl = DirectionsUtils.buildUrlFromLatLng(
                LatLng(meetingRequest.requesterLatitude, meetingRequest.requesterLongitude),
                LatLng(meetingRequest.meetingPointLatitude, meetingRequest.meetingPointLongitude)
            )
            map.getDirectionsToAbsoluteMidpoint(otherPersonUrl, meetingRequest.meetingPointName, clearPrevious = false)
        } else {
            val otherPersonUrl = DirectionsUtils.buildUrlFromLatLng(
                LatLng(meetingRequest.receiverLatitude!!, meetingRequest.receiverLongitude!!),
                LatLng(meetingRequest.meetingPointLatitude, meetingRequest.meetingPointLongitude)
            )
            map.getDirectionsToAbsoluteMidpoint(otherPersonUrl, meetingRequest.meetingPointName, clearPrevious = false)
        }*/
    }

    private fun getRequests() {
        apiController.get(MEETING_REQUEST_LIST, localUser.token) { response ->
            try {
                // todo handle empty response
                if (response == null) {
                    throw Exception("Response is empty")
                }

                val meetingResponse =
                    Gson().fromJson(response.toString(), MeetingRequestResponse::class.java)
                MeetingsSingleton.updateMeetingRequests(meetingResponse.requests)
                MeetingsSingleton.updateMeetingRequestRows(
                    MeetingUtils.sortRequests(
                        meetingResponse.requests,
                        localUser.user
                    )
                )
                initializeRecyclerView()
            } catch (e: Exception) {
                Log.e("MEETING", "$e")
            }
        }
    }

    private fun initializeRecyclerView() {
        view!!.requests_view.adapter =
            MeetingRequestsAdapter(
                (activity as MainActivity),
                this
            )
        view!!.requests_view.layoutManager = LinearLayoutManager((activity as MainActivity))
        val swipeHandler = object : SwipeToDeleteCallback(activity!!) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = view!!.requests_view.adapter as MeetingRequestsAdapter
                val removed = adapter.removeAt(viewHolder.adapterPosition)
                deleteRequest(removed.meetingRequest!!)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(view!!.requests_view)
    }

    private fun sendRequest(username: String, status: Int) {
        val body = JSONObject()
        body.put("receiver", username)
        body.put("lat", currentLocation!!.latitude)
        body.put("lng", currentLocation!!.longitude)
        body.put("status", status)
        apiController.post(
            MEETING_REQUEST_URL, body, localUser.token
        ) { response ->
            try {
                Log.d("RES", "$response")
                val msg =
                    if (response?.optString("msg").isNullOrEmpty()) {
                        response?.getString("errors")
                    } else response?.getString("msg")
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    msg ?: "Big bug",
                    Snackbar.LENGTH_LONG
                ).show()
                getRequests()
            } catch (e: IOException) {
                Log.e("MEETING", "$e")
            }
        }
    }

    override fun deleteRequest(meetingRequest: MeetingRequest) {
        val body = JSONObject()
        body.put("requestId", meetingRequest.id)
        apiController.post(
            MEETING_REQUEST_DELETE, body, localUser.token
        ) { response ->
            try {
                Log.d("RES", "$response")
                val msg =
                    if (response?.optString("msg").isNullOrEmpty()) {
                        response?.getString("errors")
                    } else response?.getString("msg")
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    msg ?: "Bug",
                    Snackbar.LENGTH_LONG
                ).show()
                val adapter = view!!.requests_view.adapter as MeetingRequestsAdapter
                val index =
                    MeetingsSingleton.meetingRequestRows.indexOfFirst { it.meetingRequest == meetingRequest }
                if (index != -1) {
                    adapter.removeAt(index)
                }
                getRequests()
            } catch (e: IOException) {
                Log.e("MEETING", "$e")
            }
        }
    }

    override fun declineRequest(meetingRequest: MeetingRequest) {
        val body = JSONObject()
        body.put("requestId", meetingRequest.id)
        apiController.post(
            MEETING_REQUEST_DECLINE, body, localUser.token
        ) { response ->
            try {
                val msg =
                    if (response?.optString("msg").isNullOrEmpty()) {
                        response?.getString("errors")
                    } else response?.getString("msg")
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    msg!!,
                    Snackbar.LENGTH_LONG
                ).show()
                getRequests()
            } catch (e: IOException) {
                Log.e("MEETING", "$e")
            }
        }
    }

    private val mLocalBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACCEPT_MEETING_REQUEST -> {
                    val request = intent.getStringExtra(EXTRA_NOTIFICATION_ID)
                    if (request != null) {
                        try {
                            val meetingRequest =
                                Gson().fromJson(request, MeetingRequest::class.java)
                            acceptRequest(meetingRequest)
                        } catch (throwable: Throwable) {
                            throwable.printStackTrace()
                        }
                    }
                }
                DECLINE_MEETING_REQUEST -> {
                    val request = intent.getStringExtra(EXTRA_NOTIFICATION_ID)
                    if (request != null) {
                        try {
                            val meetingRequest =
                                Gson().fromJson(request, MeetingRequest::class.java)
                            declineRequest(meetingRequest)
                        } catch (throwable: Throwable) {
                            throwable.printStackTrace()
                        }
                    }
                }
                UPDATE_MEETING_REQUESTS -> {
                    getRequests()
                }
            }
        }
    }

    private fun getLocalIntentFilter(): IntentFilter {
        val iFilter = IntentFilter()
        iFilter.addAction(ACCEPT_MEETING_REQUEST)
        iFilter.addAction(DECLINE_MEETING_REQUEST)
        iFilter.addAction(UPDATE_MEETING_REQUESTS)
        return iFilter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        LocalBroadcastManager.getInstance(context!!.applicationContext)
            .unregisterReceiver(mLocalBroadcastReceiver)
    }

    private fun createDialog() {
        val dialogView = layoutInflater.inflate(R.layout.request_dialog, null)
        var selectedUser = ""
        val dialog = AlertDialog.Builder(activity)
            .setView(dialogView).show()
        val chipGroup = dialog.chip_group
        val sendBtn = dialogView.send_btn
        val filterInput = dialogView.input_filter_friends
        sendBtn.isEnabled = false
        val friends = mutableListOf<Chip>()
        dialog.friends_spinner.visibility = View.VISIBLE
        apiController.get(FRIENDS_LIST, localUser.token) { res ->
            try {
                if (res == null) {
                    throw Exception("Failed to connect")
                }
                val friendsRes = Gson().fromJson(res.toString(), Friends::class.java)
                Log.d("HMM", friendsRes.toString())
                if (friendsRes.friends != null) {
                    friendsRes.friends.forEach {
                        val chip = createChip(it.username)
                        friends.add(chip)
                        chipGroup.addView(chip)
                    }
                    dialog.friends_spinner.visibility = View.GONE
                }
            } catch (e: IOException) {
                Log.e("FRIENDS", "$e")
            }
        }
        filterInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                val filteredFriends = friends.filter { it.text.contains(p0.toString()) }
                chipGroup.removeAllViews()
                filteredFriends.forEach { chipGroup.addView(it) }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        chipGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId != -1) {
                val chip = group.findViewById<Chip>(checkedId)
                selectedUser = chip.text as String
                sendBtn.isEnabled = true
            } else {
                sendBtn.isEnabled = false
            }
        }
        dialogView.cancel_btn.setOnClickListener {
            dialog.cancel()
        }
        sendBtn.setOnClickListener {
            Log.d("SWITCH", dialog.location_switch.isChecked.toString())
            sendRequest(selectedUser, if (dialog.location_switch.isChecked) 3 else 0)
            dialog.cancel()
        }
    }

    private fun createChip(username: String): Chip {
        val drawable = ChipDrawable.createFromAttributes(
            activity!!,
            null,
            0,
            R.style.Widget_MaterialComponents_Chip_Action
        )
        val chip = Chip(activity)
        chip.setChipDrawable(drawable)
        chip.text = username
        chip.setTextColor(activity!!.getColor(R.color.color_dark))
        chip.chipIcon = activity!!.getDrawable(R.drawable.ic_avatar_ph)
        chip.checkedIcon = activity!!.getDrawable(R.drawable.ic_avatar_ph)
        chip.chipBackgroundColor = activity!!.getColorStateList(R.color.chip_bg_color)
        chip.isCheckable = true
        return chip
    }

    fun arrived(meetingRequest: MeetingRequest) {
        val username =
            if (meetingRequest.requester == localUser.user.id) meetingRequest.receiverUsername else meetingRequest.requesterUsername
        MaterialAlertDialogBuilder(context)
            .setTitle("You arrived!")
            .setMessage("Send notification to ${username}?")
            .setPositiveButton("Yes", sendArrivedListener(meetingRequest))
            .setNegativeButton("No", sendArrivedListener(meetingRequest))
            .show()
    }

    private fun sendArrivedListener(meetingRequest: MeetingRequest): DialogInterface.OnClickListener {
        return DialogInterface.OnClickListener { p0, p1 ->
            val sendNotification = if (p1 == -1) 1 else 0
            sendArrived(meetingRequest.id, sendNotification)
        }
    }

    private fun sendArrived(id: String, notify: Int) {
        val body = JSONObject()
        body.put("requestId", id)
        body.put("notify", notify)
        apiController.post(MEETING_ARRIVED, body, localUser.token) { resp ->
            if (!resp?.getString("msg").isNullOrEmpty()) {
                if (notify == 1) {
                    Snackbar.make(
                        activity!!.findViewById<View>(android.R.id.content),
                        "Notification sent!",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                getRequests()
            }
        }
    }
}
