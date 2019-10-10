package com.nopoint.midpoint.models

import com.google.gson.annotations.SerializedName


data class FriendToken(
    @SerializedName("token")val token: String
)

data class ReceivedFriendRequest(
    @SerializedName("_id") val _id: String,
    @SerializedName("requester") val requester: String,
    @SerializedName("receiver") val receiver: String,
    @SerializedName("status") val status: Int,
    @SerializedName("requester_username") val req_username: String
)

data class SentFriendRequest(
    @SerializedName("_id") val _id: String,
    @SerializedName("requester") val requester: String,
    @SerializedName("receiver") val receiver: String,
    @SerializedName("status") val status: Int,
    @SerializedName("receiver_username") val rec_username: String
)


data class
FriendRequestRespond(
    @SerializedName("success") val success: Boolean
)

data class SentFriendReqRes(
    @SerializedName("success")val success: Boolean,
    @SerializedName("_id") val _id: String,
    @SerializedName("requester") val requester_id: String,
    @SerializedName("receiver") val receiver_id: String,
    @SerializedName("status") val status: Int
)

data class Friends(
    @SerializedName("friends")val friends: ArrayList<Friend>?,
    @SerializedName("received_requests")val received_requests: ArrayList<ReceivedFriendRequest>?,
    @SerializedName("sent_requests")val sent_requests: ArrayList<SentFriendRequest>?
)

data class Friend(
    @SerializedName("_id") val _id: String,
    @SerializedName("username")val username: String
)

data class FriendSearchResponse(
    @SerializedName("success")val success: Boolean,
    @SerializedName("friends")val friends: ArrayList<String>
)


