package com.nopoint.midpoint.fragments


import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.nopoint.midpoint.MeetingRequestsAdapter
import com.nopoint.midpoint.R
import com.nopoint.midpoint.map.Directions
import com.nopoint.midpoint.models.LoginResponse
import com.nopoint.midpoint.models.MeetingRequest
import com.nopoint.midpoint.networking.API
import com.nopoint.midpoint.networking.APIController
import com.nopoint.midpoint.networking.ServiceVolley
import kotlinx.android.synthetic.main.fragment_meeting.*
import kotlinx.android.synthetic.main.fragment_meeting.view.*
import kotlinx.android.synthetic.main.map_content.*
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
    private var meetingRequests = mutableListOf<MeetingRequest>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_meeting, container, false)
        getRequests()
        view.request_btn.setOnClickListener { sendRequest() }
        view.requests_view.adapter =
            MeetingRequestsAdapter(meetingRequests, activity!!, ::onResponseSent)
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
        params.put("response", 1)
        Log.d("MEET", meetingRequest.toString())
        apiController.post(API.LOCAL_API, path, params, getToken()) { response ->
            try {
                Log.d("RES", "$response")
                Snackbar.make(activity!!.findViewById(android.R.id.content), response!!.get("msg").toString(), Snackbar.LENGTH_LONG).show()
                val map = parentFragment as MapFragment
                map.getDirections(destinationCoord = middlePoint)
            } catch (e: IOException) {
                Log.e("MEETING", "$e")
            }
        }
    }

    private fun getRequests() {
        val path = "/meeting-request/all"
        apiController.get(API.LOCAL_API, path, getToken()) { response ->
            try {
                Log.d("RES", "$response")
                val requests = response!!.getJSONArray("requests") as JSONArray
                for (i in 0 until requests.length()) {
                    val request =
                        Gson().fromJson(requests[i].toString(), MeetingRequest::class.java)
                    meetingRequests.add(request)
                }
                requests_view.adapter?.notifyDataSetChanged()
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
                Log.d("RES", "$response")

            } catch (e: IOException) {
                Log.e("MEETING", "$e")
            }
        }
    }

    private fun getToken(): String {
        Log.d("GET TOKEN", "start")
        val prefs = this.activity!!.getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""
        Log.d("TOKEN", token)
        return token
    }

}
