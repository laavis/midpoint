package com.nopoint.midpoint.models

import com.google.gson.annotations.SerializedName
import java.util.*
import kotlin.collections.ArrayList

data class MeetingRequestResponse(
    @SerializedName("requests") val requests: ArrayList<MeetingRequest>
)

data class MeetingRequest(
    @SerializedName("_id") val id: String,
    @SerializedName("requester") val requester: String,
    @SerializedName("requesterUsername") var requesterUsername: String,
    @SerializedName("receiver") val receiver: String,
    @SerializedName("receiverUsername") var receiverUsername: String,
    @SerializedName("status") val status: Int,
    @SerializedName("requesterLat") val requesterLatitude: Double,
    @SerializedName("requesterLng") val requesterLongitude: Double,
    @SerializedName("recieverLat") val receiverLatitude: Double?,
    @SerializedName("recieverLng") val receiverLongitude: Double?,
    @SerializedName("meetingPointLat") val meetingPointLatitude: Double?,
    @SerializedName("meetingPointLng") val meetingPointLongitude: Double?,
    @SerializedName("meetingPointName") val meetingPointName: String?,
    @SerializedName("timestamp") val timestamp: Date
)

data class MeetingRequestRow(val meetingRequest: MeetingRequest?, val type: MeetingType?, val rowType: RowType)

enum class MeetingType(val type: Int){
    ACTIVE(0),
    INCOMING(1),
    REJECTED(2),
    OUTGOING(3),
}

enum class RowType{
    HEADER,
    REQUEST,
    DELETABLE
}