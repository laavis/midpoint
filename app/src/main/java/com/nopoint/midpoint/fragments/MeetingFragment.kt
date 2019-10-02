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
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.nopoint.midpoint.MainActivity
import com.nopoint.midpoint.MeetingRequestsAdapter
import com.nopoint.midpoint.R
import com.nopoint.midpoint.map.Directions
import com.nopoint.midpoint.map.MeetingUtils
import com.nopoint.midpoint.models.*
import com.nopoint.midpoint.networking.API
import com.nopoint.midpoint.networking.APIController
import com.nopoint.midpoint.networking.ServiceVolley
import kotlinx.android.synthetic.main.fragment_meeting.view.*
import org.json.JSONObject
import java.io.IOException

/**
 * A simple [Fragment] subclass.
 */
class MeetingFragment : Fragment() {
    private val service = ServiceVolley()
    private val apiController = APIController(service)

    //todo rename to recCoord as receiver coordinates
    var currentLocation: Location? = null



    private var meetingRequests = mutableListOf<MeetingRequestRow>()
    private lateinit var localUser: LocalUser

    private lateinit var userLocationRequest: LocationRequest

    private lateinit var meetingUsernameInput: TextInputEditText

    private var hasGps = false
    private var hasNetwork = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_meeting, container, false)
        meetingUsernameInput = view.findViewById(R.id.meeting_username_input)

        view.request_btn.setOnClickListener {

            if(meetingUsernameInput.text!!.isNotEmpty()) {
                val reqString = meetingUsernameInput.text.toString()
                sendRequest(reqString)
            }
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        localUser = CurrentUser.getLocalUser(activity!!)!!
        getRequests()
    }


    // RESPOND TO MEETING REQUEST
    private fun onResponseSent(meetingRequest: MeetingRequest) {
        val params = JSONObject()
        val path = "/meeting-request/respond"
        val requesterLoc = Location("")
        requesterLoc.longitude = meetingRequest.requesterLongitude
        requesterLoc.latitude = meetingRequest.requesterLatitude
        val middlePoint = Directions.getMiddlePoint(currentLocation!!, requesterLoc)

        var latestLocation = currentLocation

        Log.d("COORD | RECEIVER ON RESPOND", "lat: ${latestLocation!!.latitude}, lng: ${latestLocation!!.longitude}")


        params.put("requestId", meetingRequest.id)
        params.put("lat", currentLocation!!.latitude)
        params.put("lng", currentLocation!!.longitude)
        params.put("middleLat", middlePoint.latitude)
        params.put("middleLng", middlePoint.longitude)
        params.put("response", 1) //TODO allow setting different response types

        Log.d("MEET", meetingRequest.toString())
        apiController.post(API.LOCAL_API, path, params, localUser.token) { response ->
            try {
                Log.d("RES", "$response")
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    response!!.get("msg").toString(),
                    Snackbar.LENGTH_LONG
                ).show()
                val map = parentFragment as MapFragment
                map.getDirections(destinationCoord = requesterLoc)
            } catch (e: IOException) {
                Log.e("MEETING", "$e")
            }
        }
    }

    private fun showOnMap(meetingRequest: MeetingRequest) {
        val middlePoint = Location("")
        middlePoint.latitude = meetingRequest.meetingPointLatitude!!
        middlePoint.longitude = meetingRequest.meetingPointLongitude!!

        val fullRoute = Location("")
        fullRoute.latitude = meetingRequest.requesterLatitude
        fullRoute.longitude = meetingRequest.requesterLongitude
        val map = parentFragment as MapFragment
        map.getDirections(destinationCoord = fullRoute)
    }


    private fun sendRequest(username: String) {
        val params = JSONObject()
        val path = "/meeting-request/request"

        val latestLocation = currentLocation

        Log.d("COORD | REQUESTER", "lat: ${latestLocation!!.latitude}, lng: ${latestLocation!!.longitude}")
        params.put("receiver", username)
        params.put("lat", latestLocation!!.latitude)
        params.put("lng", latestLocation!!.longitude)
        apiController.post(API.LOCAL_API, path, params, localUser.token) { response ->
            try {
                //TODO Refresh recycler view with new request
                Log.d("RES", "$response")

            } catch (e: IOException) {
                Log.e("MEETING", "$e")
            }
        }
    }

    private fun getRequests() {
        val path = "/meeting-request/all"
        apiController.get(API.LOCAL_API, path, localUser.token) { response ->
            try {
                Log.d("RES", "$response")
                val meetingResponse =
                    Gson().fromJson(response.toString(), MeetingRequestResponse::class.java)
                meetingRequests =
                    MeetingUtils.sortRequests(meetingResponse.requests, localUser.user)
                // Was causing a crash
                if (view != null && activity != null){
                    initializeRecyclerView()
                }
            } catch (e: IOException) {
                Log.e("MEETING", "$e")
            }
        }
    }

    // init
    private fun initializeRecyclerView() {
        view!!.requests_view.adapter =
            MeetingRequestsAdapter(meetingRequests, (activity as MainActivity), ::onResponseSent, ::showOnMap)
        view!!.requests_view.layoutManager = LinearLayoutManager((activity as MainActivity))
        val dividerItemDecoration = DividerItemDecoration(
            view!!.requests_view.context,
            (view!!.requests_view.layoutManager as LinearLayoutManager).orientation
        )
        view!!.requests_view.addItemDecoration(dividerItemDecoration)
    }
}
