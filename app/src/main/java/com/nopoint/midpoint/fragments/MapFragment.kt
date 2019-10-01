package com.nopoint.midpoint.fragments

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import com.nopoint.midpoint.R
import com.nopoint.midpoint.map.Directions
import com.nopoint.midpoint.map.MapsFactory
import com.nopoint.midpoint.map.models.Route
import com.nopoint.midpoint.networking.API
import com.nopoint.midpoint.networking.APIController
import com.nopoint.midpoint.networking.ServiceVolley
import kotlinx.android.synthetic.main.map_content.view.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.nopoint.midpoint.MainActivity
import kotlinx.android.synthetic.main.bottom_sheet.view.*

/**
 * A simple [Fragment] subclass.
 */
class MapFragment : Fragment(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private var mapFragment: SupportMapFragment? = null
    private var requestingLocationUpdates = false
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var locationCallback: LocationCallback
    private var mRouteMarkerList = ArrayList<Marker>()
    private lateinit var mRoutePolyline: Polyline
    private val service = ServiceVolley()
    private val apiController = APIController(service)
    private var sheetBehavior: BottomSheetBehavior<*>? = null
    private var state = BottomSheetBehavior.STATE_COLLAPSED

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        if (mapFragment == null){
            mapFragment = SupportMapFragment.newInstance()
            mapFragment!!.getMapAsync(this)
        }

        (activity as MainActivity).supportActionBar?.title = "Map"

        childFragmentManager.beginTransaction().replace(R.id.google_map, mapFragment!!).commit()
        getToken()
        view.directions_btn.setOnClickListener {
            if (view.destination_txt.text.isNotBlank()) {
                getDirections(destination = view.destination_txt.text.toString())
            } else {
                //Test coordinates for helsinki
                //TODO actually get friend's coordinates from API
                val dest = Location("")
                dest.latitude = 60.1696327
                dest.longitude = 24.9369516
                getDirections(destinationCoord = dest)
            }
        }
        sheetBehavior = BottomSheetBehavior.from(view.bottom_sheet)
        sheetBehavior!!.bottomSheetCallback = createBottomSheetCb()
        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (!requestingLocationUpdates) {
            mMap.isMyLocationEnabled = true
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
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
        val prefs = this.activity!!.getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        Log.d("TOKEN", token)
    }

    private fun createBottomSheetCb(): BottomSheetBehavior.BottomSheetCallback{
        return object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(view: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> state = BottomSheetBehavior.STATE_HIDDEN
                    BottomSheetBehavior.STATE_EXPANDED -> state = BottomSheetBehavior.STATE_EXPANDED
                    BottomSheetBehavior.STATE_COLLAPSED -> state = BottomSheetBehavior.STATE_COLLAPSED
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        if (state != BottomSheetBehavior.STATE_HALF_EXPANDED) {
                            sheetBehavior!!.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                        }
                    }
                    BottomSheetBehavior.STATE_SETTLING -> state = BottomSheetBehavior.STATE_SETTLING
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> state = BottomSheetBehavior.STATE_HALF_EXPANDED
                }
            }
            override fun onSlide(view: View, v: Float) {}
        }
    }

    private fun createLocationCallback(): LocationCallback {
        return object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    if (!requestingLocationUpdates) {
                        val loc = LatLng(location.latitude, location.longitude)
                        val cam = CameraUpdateFactory.newLatLngZoom(loc, 15.0f)
                        val sheetFragment = childFragmentManager.findFragmentById(R.id.fragment) as MeetingFragment
                        sheetFragment.currentLocation = location
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

    fun getDirections(
        destination: String = "Helsinki",
        destinationCoord: Location? = null
    ) {
        if (mRouteMarkerList.isNotEmpty()) clearMarkersAndRoute()
        if (state == BottomSheetBehavior.STATE_EXPANDED){
            sheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
        }
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
                .icon(BitmapDescriptorFactory.fromBitmap(MapsFactory.drawMarker(this.activity!!, "")))
        val endLatLng = LatLng(route.endLat!!, route.endLng!!)
        val endMarkerOptions: MarkerOptions =
            MarkerOptions().position(endLatLng).title(route.endName).icon(
                BitmapDescriptorFactory.fromBitmap(MapsFactory.drawMarker(this.activity!!, ""))
            )
        val startMarker = mMap.addMarker(startMarkerOptions)
        val endMarker = mMap.addMarker(endMarkerOptions)
        mRouteMarkerList.add(startMarker)
        mRouteMarkerList.add(endMarker)

        val polylineOptions = MapsFactory.drawRoute(this.activity!!)
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
