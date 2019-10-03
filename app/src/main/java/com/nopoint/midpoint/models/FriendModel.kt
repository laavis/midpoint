package com.nopoint.midpoint.models

import com.google.gson.annotations.SerializedName

data class FriendRequest(
    @SerializedName("_id") val id: String,
    @SerializedName("requester") val requester_id: String,
    @SerializedName("receiver") val receiver_id: String,
    @SerializedName("status") val status: Int
)


