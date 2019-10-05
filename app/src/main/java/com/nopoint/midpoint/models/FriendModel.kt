package com.nopoint.midpoint.models

import com.google.gson.annotations.SerializedName

data class FriendRequestResponse(
    @SerializedName("success")val success: Boolean,
    @SerializedName("_id") val id: String,
    @SerializedName("requester") val requester_id: String,
    @SerializedName("receiver") val receiver_id: String,
    @SerializedName("status") val status: Int
)


