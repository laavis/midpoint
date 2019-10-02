package com.nopoint.midpoint.models

import com.google.gson.annotations.SerializedName

data class UserSearchResponse(
    @SerializedName("users")val users: List<UserSearchResponseUser>
)

data class UserSearchResponseUser(
    @SerializedName("_id")val _id: String,
    @SerializedName("username")val username: String
)
