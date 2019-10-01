package com.nopoint.midpoint.models

import com.google.gson.annotations.SerializedName


// todo: probably move this to own file
data class User (
    @SerializedName("_id")val id: String,
    @SerializedName("username")val username: String,
    @SerializedName("email")val email: String,
    @SerializedName("password")val password: String,
    @SerializedName("friendsList")val friendsList: ArrayList<String>
)

data class SignUpResponse(
    @SerializedName("success")val success: Boolean,
    @SerializedName("user")val user: User
)

data class SignUpErrorResponse(
    @SerializedName("username")val username: String,
    @SerializedName("email")val email: String,
    @SerializedName("password")val password: String,
    @SerializedName("confirm_password")val confirm_password: String
)

data class LoginResponse(
    @SerializedName("success")val success: Boolean,
    @SerializedName("user")val user: User,
    @SerializedName("token")val token: String
)

data class LoginErrorResponse (
    @SerializedName("email")val email: String,
    @SerializedName("password")val password: String
)

data class LocalUser(
    @SerializedName("user")val user:User,
    @SerializedName("token")val token: String,
    @SerializedName("expiration")val expiration: Long)