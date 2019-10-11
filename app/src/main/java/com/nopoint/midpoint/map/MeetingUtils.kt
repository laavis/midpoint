package com.nopoint.midpoint.map

import android.location.Location
import android.util.Log
import com.nopoint.midpoint.models.*


object MeetingUtils {

    /**
     * Sorts requests into the correct order by meeting request status
     * Active first, then received and finally sent requests
     * @param unsortedRequests list of meeting requests
     * @param currentUser to determine meeting request type
     * @return List of meeting request row objects in correct order
     */
    fun sortRequests(unsortedRequests: List<MeetingRequest>,currentUser: User): MutableList<MeetingRequestRow> {
        val map = HashMap<MeetingType, ArrayList<MeetingRequest>>()
        for (req in unsortedRequests) {
            val title = getHeaderTitle(req, currentUser)
            var requests = map[title]
            if (requests == null) {
                requests = ArrayList()
                map[title] = requests
            }
            requests.add(req)
        }
        val sortedMap = map.toSortedMap(compareBy {it.ordinal}) //sorts by importance
        val requests = ArrayList<MeetingRequestRow>()
        for ((key, value) in sortedMap) {
            if (key != MeetingType.ACTIVE){
                requests.add(MeetingRequestRow(null, key, RowType.HEADER, false))
            }
            value.mapTo(requests) {
                MeetingRequestRow(
                    meetingRequest = it,
                    type = key,
                    rowType = if (it.requester == currentUser.id) RowType.DELETABLE else RowType.REQUEST,
                    expanded = false
                )
            }
        }
        return requests
    }

    /**
     * Sorts requests into the correct order by meeting request status
     * Active first, then received and finally sent requests
     * @param meetingRequest current active meeting request
     * @param currentLocation current location of the user
     * @param radius the set arrival radius
     * @return boolean to determine if user has arrived
     */
    fun reachedLocation(meetingRequest: MeetingRequest, currentLocation: Location, radius: Double): Boolean{
        val midpoint = Location("")
        midpoint.latitude = meetingRequest.meetingPointLatitude!!
        midpoint.longitude =  meetingRequest.meetingPointLongitude!!
        return midpoint.distanceTo(currentLocation) < radius
    }

    private fun getHeaderTitle(req: MeetingRequest, currentUser: User): MeetingType {
        return when {
            req.status == 2 -> MeetingType.REJECTED
            req.status == 1 -> MeetingType.ACTIVE
            req.receiver == currentUser.id -> MeetingType.INCOMING
            req.requester == currentUser.id -> MeetingType.OUTGOING
            else -> MeetingType.ACTIVE
        }
    }
}