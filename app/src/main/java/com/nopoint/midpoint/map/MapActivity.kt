package com.nopoint.midpoint.map

import android.content.Context
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import kotlinx.android.synthetic.main.activity_map.*
import com.nopoint.midpoint.R
import com.nopoint.midpoint.map.models.Route
import com.nopoint.midpoint.networking.APIController
import com.nopoint.midpoint.networking.API
import com.nopoint.midpoint.networking.ServiceVolley


class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private var requestingLocationUpdates = false
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var locationCallback: LocationCallback
    private var mRouteMarkerList = ArrayList<Marker>()
    private lateinit var mRoutePolyline: Polyline
    private val service = ServiceVolley()
    private val apiController = APIController(service)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        getToken()
        mapFragment.getMapAsync(this)
        directions_btn.setOnClickListener {
            if (mRouteMarkerList.isNotEmpty()) clearMarkersAndRoute()
            
            if (destination_txt.text.isNotBlank()) {
                getDirections(destination = destination_txt.text.toString())
            } else {
                //Test coordinates for helsinki
                //TODO actually get friend's coordinates from API
                val dest = Location("")
                dest.latitude = 60.1696327
                dest.longitude = 24.9369516
                getDirections(destinationCoord = dest)
            }
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (!requestingLocationUpdates) {
            mMap.isMyLocationEnabled = true
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            locationCallback = createLocationCallback()
            startLocationUpdates()
        }
    }

    override fun onResume() {
        super.onResume()
        if (requestingLocationUpdates) startLocationUpdates()
    }

    private fun getToken() {
        Log.d("GET TOKEN", "start")
        val prefs = this.getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("token", "")

        Log.d("TOKEN", token)
    }


    private fun createLocationCallback(): LocationCallback {
        return object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    if (!requestingLocationUpdates) {
                        val loc = LatLng(location.latitude, location.longitude)
                        val cam = CameraUpdateFactory.newLatLngZoom(loc, 15.0f)
                        mMap.animateCamera(cam)
                    }
                    requestingLocationUpdates = true
                }
            }
        }
    }

    private fun startLocationUpdates() {
        fusedLocationClient!!.requestLocationUpdates(
            createLocationRequest(),
            locationCallback,
            null /* Looper */
        )
    }

    private fun createLocationRequest(): LocationRequest? {
        return LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun getDirections(
        destination: String = "Helsinki",
        destinationCoord: Location? = null
    ) {
        fusedLocationClient?.lastLocation?.addOnSuccessListener { loc: Location? ->
            val url = if (destinationCoord != null)
                Directions.buildUrl(loc, Directions.getMiddlePoint(loc!!, destinationCoord))
            else
                Directions.buildUrl(loc, destination)
            apiController.get(API.DIRECTIONS, url) { response ->
                if (response != null) {
                    val route = Directions.buildRoute(response)
                    setMarkersAndRoute(route)
                }
            }
        }
    }

    private fun setMarkersAndRoute(route: Route) {
        val startLatLng = LatLng(route.startLat!!, route.startLng!!)
        val startMarkerOptions: MarkerOptions =
            MarkerOptions().position(startLatLng).title(route.startName)
                .icon(BitmapDescriptorFactory.fromBitmap(MapsFactory.drawMarker(this, "")))
        val endLatLng = LatLng(route.endLat!!, route.endLng!!)
        val endMarkerOptions: MarkerOptions =
            MarkerOptions().position(endLatLng).title(route.endName).icon(
                BitmapDescriptorFactory.fromBitmap(MapsFactory.drawMarker(this, ""))
            )
        val startMarker = mMap.addMarker(startMarkerOptions)
        val endMarker = mMap.addMarker(endMarkerOptions)
        mRouteMarkerList.add(startMarker)
        mRouteMarkerList.add(endMarker)

        val polylineOptions = MapsFactory.drawRoute(this)
        val pointsList = PolyUtil.decode(route.overviewPolyline)
        for (point in pointsList) {
            polylineOptions.add(point)
        }

        mRoutePolyline = mMap.addPolyline(polylineOptions)

        mMap.animateCamera(MapsFactory.autoZoomLevel(mRouteMarkerList))
    }

    private fun clearMarkersAndRoute() {
        for (marker in mRouteMarkerList) {
            marker.remove()
        }
        mRouteMarkerList.clear()

        if (::mRoutePolyline.isInitialized) {
            mRoutePolyline.remove()
        }
    }
}
