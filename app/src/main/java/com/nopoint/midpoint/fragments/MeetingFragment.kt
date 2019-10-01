package com.nopoint.midpoint.fragments


import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.nopoint.midpoint.MeetingRequestsAdapter
import com.nopoint.midpoint.R
import com.nopoint.midpoint.map.Directions
import com.nopoint.midpoint.models.*
import com.nopoint.midpoint.networking.API
import com.nopoint.midpoint.networking.APIController
import com.nopoint.midpoint.networking.ServiceVolley
import kotlinx.android.synthetic.main.fragment_meeting.*
import kotlinx.android.synthetic.main.fragment_meeting.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

/**
 * A simple [Fragment] subclass.
 */
class MeetingFragment : Fragment() {
    private val service = ServiceVolley()
    private val apiController = APIController(service)
    var currentLocation: Location? = null
    private var meetingRequests = mutableListOf<MeetingRequestRow>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_meeting, container, false)
        getRequests()
        view.request_btn.setOnClickListener { sendRequest(view.friend_username.text.toString()) }
        view.requests_view.adapter =
            MeetingRequestsAdapter(meetingRequests, activity!!, ::onResponseSent, ::showOnMap)
        view.requests_view.layoutManager = LinearLayoutManager(activity!!)
        val dividerItemDecoration = DividerItemDecoration(
            view.requests_view.context,
            (view.requests_view.layoutManager as LinearLayoutManager).orientation
        )
        view.requests_view.addItemDecoration(dividerItemDecoration)
        return view
    }

    private fun onResponseSent(meetingRequest: MeetingRequest) {
        val params = JSONObject()
        val path = "/meeting-request/respond"
        val requesterLoc = Location("")
        requesterLoc.longitude = meetingRequest.requesterLongitude
        requesterLoc.latitude = meetingRequest.requesterLatitude
        val middlePoint = Directions.getMiddlePoint(currentLocation!!, requesterLoc)

        params.put("requestId", meetingRequest.id)
        params.put("lat", currentLocation!!.latitude)
        params.put("lng", currentLocation!!.longitude)
        params.put("middleLat", middlePoint.latitude)
        params.put("middleLng", middlePoint.longitude)
        params.put("response", 1) //TODO allow setting different response types

        Log.d("MEET", meetingRequest.toString())
        apiController.post(API.LOCAL_API, path, params, getToken()) { response ->
            try {
                Log.d("RES", "$response")
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    response!!.get("msg").toString(),
                    Snackbar.LENGTH_LONG
                ).show()
                val map = parentFragment as MapFragment
                map.getDirections(destinationCoord = middlePoint)
            } catch (e: IOException) {
                Log.e("MEETING", "$e")
            }
        }
    }
    private fun showOnMap(meetingRequest: MeetingRequest){
        val middlePoint = Location("")
        middlePoint.latitude = meetingRequest.meetingPointLatitude!!
        middlePoint.longitude = meetingRequest.meetingPointLongitude!!
        val map = parentFragment as MapFragment
        map.getDirections(destinationCoord = middlePoint)
    }

    private fun getRequests() {
        val path = "/meeting-request/all"
        val reqs = mutableListOf<MeetingRequest>()
        apiController.get(API.LOCAL_API, path, getToken()) { response ->
            try {
                Log.d("RES", "$response")
                val requests = response!!.getJSONArray("requests") as JSONArray
                for (i in 0 until requests.length()) {
                    val request =
                        Gson().fromJson(requests[i].toString(), MeetingRequest::class.java)
                    reqs.add(request)
                }
                meetingRequests = sortRequests(reqs, getCurrentUser())
                val newAdapter = MeetingRequestsAdapter(meetingRequests, activity!!, ::onResponseSent, ::showOnMap)
                requests_view.swapAdapter(newAdapter,false)
            } catch (e: IOException) {
                Log.e("MEETING", "$e")
            }
        }
    }

    private fun sendRequest(username: String = "testpete") {
        val params = JSONObject()
        val path = "/meeting-request/request"
        params.put("receiver", username)
        params.put("lat", currentLocation!!.latitude)
        params.put("lng", currentLocation!!.longitude)
        apiController.post(API.LOCAL_API, path, params, getToken()) { response ->
            try {
                //TODO Refresh recycler view with new request
                Log.d("RES", "$response")

            } catch (e: IOException) {
                Log.e("MEETING", "$e")
            }
        }
    }

    private fun sortRequests(
        unsortedRequests: List<MeetingRequest>,
        currentUser: User
    ): MutableList<MeetingRequestRow> {
        val map = HashMap<MeetingType, ArrayList<MeetingRequest>>()
        for (req in unsortedRequests) {
            val title = getHeaderTitle(req, currentUser)
            var reqs = map[title]
            if (reqs == null) {
                reqs = ArrayList()
                map[title] = reqs
            }
            reqs.add(req)
        }
        val requests = ArrayList<MeetingRequestRow>()
        for ((key, value) in map) {
            requests.add(MeetingRequestRow(null, key, RowType.HEADER))
            value.mapTo(requests) { MeetingRequestRow(it, key, RowType.REQUEST) }
        }
        return requests
    }

    private fun getHeaderTitle(req: MeetingRequest, currentUser: User): MeetingType {
        return when {
            req.status == 2 -> {
                MeetingType.REJECTED
            }
            req.status != 0 -> {
                MeetingType.ACTIVE
            }
            req.receiver == currentUser.id -> {
                MeetingType.INCOMING
            }
            req.requester == currentUser.id -> {
                MeetingType.OUTGOING
            }
            else -> MeetingType.ACTIVE
        }
    }

    private fun getToken(): String {
        Log.d("GET TOKEN", "start")
        val prefs = this.activity!!.getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""
        Log.d("TOKEN", token)
        return token
    }

    private fun getCurrentUser(): User {
        //TODO get user from shared prefs / fetch from server
        return User("5d8b6b2ae11065074b882035", "aa", "a@a.com", "", arrayListOf())
    }

}
