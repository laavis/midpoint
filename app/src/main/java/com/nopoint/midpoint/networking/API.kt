package com.nopoint.midpoint.networking

enum class API(val path: String) {
    PLACES("https://maps.googleapis.com/maps/api/place/nearbysearch/json"),
    DIRECTIONS("https://maps.googleapis.com/maps/api/directions/json"),
    BASE("http://localhost:5000"),//TODO Set real API endpoint
    TEST("https://postman-echo.com/"),
    LOCAL_API("http://198.18.11.162:5000")
}

const val BASE_URL = "http://198.18.9.114:5000"
const val LOGIN_URL = "/user/login"
const val MEETING_REQUEST_URL = "/meeting-request/request"
const val MEETING_RESPOND_URL = "/meeting-request/respond"
const val FRIEND_SEND_REQUEST_URL = "/friends/request"
const val FRIENDS_LIST = "/friends/list"
const val FRIENDS_RESPOND = "/friends/respond"
const val MEETING_REQUEST_LIST = "/meeting-request/all"
const val MEETING_REQUEST_DELETE = "/meeting-request/delete"
const val MEETING_REQUEST_DECLINE = "/meeting-request/decline"