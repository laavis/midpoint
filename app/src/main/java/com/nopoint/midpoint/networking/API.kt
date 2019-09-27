package com.nopoint.midpoint.networking

enum class API(val path: String) {
    PLACES("https://maps.googleapis.com/maps/api/places/json"),
    DIRECTIONS("https://maps.googleapis.com/maps/api/directions/json"),
    BASE("http://localhost:5000"),//TODO Set real API endpoint
    TEST("https://postman-echo.com/"),
    LOCAL_API("http://192.168.1.107:5000")
}