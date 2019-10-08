package com.nopoint.midpoint.map

import com.nopoint.midpoint.models.*

object MeetingUtils {

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

    private fun getHeaderTitle(req: MeetingRequest, currentUser: User): MeetingType {
        return when {
            req.status == 2 -> MeetingType.REJECTED
            req.status != 0 -> MeetingType.ACTIVE
            req.receiver == currentUser.id -> MeetingType.INCOMING
            req.requester == currentUser.id -> MeetingType.OUTGOING
            else -> MeetingType.ACTIVE
        }
    }
}