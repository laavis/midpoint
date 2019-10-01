package com.nopoint.midpoint.models

import com.google.gson.annotations.SerializedName

data class UserSearchResponse(
    @SerializedName("users")val users: ArrayList<String>
)
