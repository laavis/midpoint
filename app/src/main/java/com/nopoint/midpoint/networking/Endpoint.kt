package com.nopoint.midpoint.networking

enum class Endpoint(val path: String) {
    PLACES("https://maps.googleapis.com/maps/api/places/json"),
    DIRECTIONS("https://maps.googleapis.com/maps/api/directions/json"),
    BASE(""),//TODO Set real API endpoint
    TEST("https://postman-echo.com/")
}