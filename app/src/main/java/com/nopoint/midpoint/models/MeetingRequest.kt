package com.nopoint.midpoint.models

import com.google.gson.annotations.SerializedName

data class MeetingRequest(
    @SerializedName("_id") val id: String,
    @SerializedName("requester") val requester: String,
    @SerializedName("receiver") val receiver: String,
    @SerializedName("status") val status: Int,
    @SerializedName("requesterLat") val requesterLatitude: Double,
    @SerializedName("requesterLng") val requesterLongitude: Double,
    @SerializedName("recieverLat") val receiverLatitude: Double?,
    @SerializedName("recieverLng") val receiverLongitude: Double?,
    @SerializedName("meetingPointLat") val meetingPointLatitude: Double?,
    @SerializedName("meetingPointLng") val meetingPointLongitude: Double?
)