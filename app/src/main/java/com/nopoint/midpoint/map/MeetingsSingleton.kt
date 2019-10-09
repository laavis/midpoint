package com.nopoint.midpoint.map

import com.nopoint.midpoint.models.MeetingRequest
import com.nopoint.midpoint.models.MeetingRequestRow

object MeetingsSingleton {
    val meetingRequests: MutableList<MeetingRequest> = mutableListOf()
    val meetingRequestRows: MutableList<MeetingRequestRow> = mutableListOf()


    fun getActiveMeeting(): MeetingRequest?{
        return meetingRequests.find { it.status == 1 }
    }

    fun updateMeetingRequestRows(newRows: MutableList<MeetingRequestRow>) {
        meetingRequestRows.clear()
        meetingRequestRows.addAll(newRows)
    }

    fun updateMeetingRequests(newRows: MutableList<MeetingRequest>) {
        meetingRequests.clear()
        meetingRequests.addAll(newRows)
    }
}