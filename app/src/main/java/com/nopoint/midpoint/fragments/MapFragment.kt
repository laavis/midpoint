package com.nopoint.midpoint.fragments

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
import com.nopoint.midpoint.map.models.FullRoute
import com.nopoint.midpoint.networking.API
import com.nopoint.midpoint.networking.APIController
import com.nopoint.midpoint.networking.ServiceVolley
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.google.maps.android.SphericalUtil
import com.nopoint.midpoint.map.models.Direction
import kotlinx.android.synthetic.main.bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_map.*
import com.google.android.gms.maps.model.LatLng


/**
 * A "simple" [Fragment] subclass.
 */

class MapFragment : Fragment(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private val service = ServiceVolley()
    private val apiController = APIController(service)
    private var mapFragment: SupportMapFragment? = null
    private var requestingLocationUpdates = false
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var locationCallback: LocationCallback
    private var mRouteMarkerList = ArrayList<Marker>()
    private lateinit var mRoutePolyline: Polyline

    private var sheetBehavior: BottomSheetBehavior<*>? = null
    private var state = BottomSheetBehavior.STATE_COLLAPSED


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance()
            mapFragment!!.getMapAsync(this)
        }

        childFragmentManager.beginTransaction().replace(R.id.google_map, mapFragment!!).commit()
        sheetBehavior = BottomSheetBehavior.from(view.bottom_sheet)
        sheetBehavior!!.bottomSheetCallback = createBottomSheetCb()

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // For testing
        val espooLatLng = LatLng(60.205498, 24.653837)
        val kauklahtiLatLng = LatLng(60.193357, 24.598792)
        val hese = LatLng(60.191205, 24.949999)

        val url = Directions.buildUrlTest(espooLatLng, kauklahtiLatLng)
        val url2 = Directions.buildUrlTest(kauklahtiLatLng, hese)


        button_TEST.setOnClickListener {
            Log.d("ROUTING", "url: $url")

            apiController.get(API.DIRECTIONS, url2) { response ->
                if (response != null) {

                    val result = Gson().fromJson(response.toString(), Direction::class.java)

                    val legs = result.routes[0].legs
                    val path = ArrayList<LatLng>()

                    for (i in 0 until legs[0].steps.size) {
                        path.addAll(PolyUtil.decode(legs[0].steps[i].polyline.points))
                    }

                    Log.d("ROUTING DISTANCE VALUE", "${legs[0].distance.value}")

                    val middle = (legs[0].distance.value / 2).toDouble()

                    val midPointCoordinates = extrapolate(path, path[0], middle)


                    val midpointURL = Directions.buildUrlTest(kauklahtiLatLng, midPointCoordinates)


                    apiController.get(API.DIRECTIONS, midpointURL) { response ->
                        if (response != null) {
                            val route = Directions.buildRoute(response)
                            setMarkersAndRoute(route)
                        }
                    }

                }
            }
        }
    }

    public fun decodePolyline(encoded: String): List<LatLng> {

        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latLng = LatLng((lat.toDouble() / 1E5), (lng.toDouble() / 1E5))
            poly.add(latLng)
        }

        return poly
    }


    private fun extrapolate(path: List<LatLng>, origin: LatLng, distance: Double): LatLng? {
        var extrapolated: LatLng? = null

        if (!PolyUtil.isLocationOnPath(origin, path, false, 10.0)) {
            return null
        }

        var accDistance = 0.0
        var foundStart = false
        val segment = ArrayList<LatLng>()


        for (i in 0 until path.size - 1) {
            val segmentStart = path[i]
            val segmentEnd = path[i + 1]

            segment.clear()
            segment.add(segmentStart)
            segment.add(segmentEnd)

            var currentDistance: Double = 0.0

            if (!foundStart) {

                if (PolyUtil.isLocationOnPath(origin, segment, false, 1.0)) {

                    foundStart = true

                    currentDistance = SphericalUtil.computeDistanceBetween(origin, segmentEnd)


                    if (currentDistance > distance) {

                        val heading: Double = SphericalUtil.computeHeading(origin, segmentEnd)
                        extrapolated =
                            SphericalUtil.computeOffset(origin, distance - accDistance, heading)
                        break
                    }
                }


            } else {
                currentDistance = SphericalUtil.computeDistanceBetween(segmentStart, segmentEnd)


                if (currentDistance + accDistance > distance) {
                    val heading: Double = SphericalUtil.computeHeading(segmentStart, segmentEnd)
                    extrapolated =
                        SphericalUtil.computeOffset(segmentStart, distance - accDistance, heading)
                    break
                }
            }

            accDistance += currentDistance
        }

        Log.d("EXTRA EXTRAPOLATED", "$extrapolated")

        return extrapolated

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


    private fun createLocationCallback(): LocationCallback {
        return object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    if (!requestingLocationUpdates) {
                        val loc = LatLng(location.latitude, location.longitude)
                        Log.d("MAP", "lat: ${location.latitude}, lng: ${location.longitude}")
                        val cam = CameraUpdateFactory.newLatLngZoom(loc, 15.0f)
                        val sheetFragment =
                            childFragmentManager.findFragmentById(R.id.fragment) as MeetingFragment
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

    private fun setMarkersAndRoute(fullRoute: FullRoute) {
        val startLatLng = LatLng(fullRoute.startLat!!, fullRoute.startLng!!)
        val startMarkerOptions: MarkerOptions =
            MarkerOptions().position(startLatLng).title(fullRoute.startName)
                .icon(
                    BitmapDescriptorFactory.fromBitmap(
                        MapsFactory.drawMarker(
                            this.activity!!,
                            ""
                        )
                    )
                )
        val endLatLng = LatLng(fullRoute.endLat!!, fullRoute.endLng!!)

        val endMarkerOptions: MarkerOptions =
            MarkerOptions().position(endLatLng).title(fullRoute.endName).icon(
                BitmapDescriptorFactory.fromBitmap(MapsFactory.drawEndMarker(this.activity!!, ""))
            )

        val startMarker = mMap.addMarker(startMarkerOptions)
        val endMarker = mMap.addMarker(endMarkerOptions)
        mRouteMarkerList.add(startMarker)
        mRouteMarkerList.add(endMarker)

        val polylineOptions = MapsFactory.drawRoute(this.activity!!)
        val pointsList = PolyUtil.decode(fullRoute.overviewPolyline)
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

    fun getDirections(destination: String = "", destinationCoord: Location?) {
        if (mRouteMarkerList.isNotEmpty()) clearMarkersAndRoute()
        if (state == BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        fusedLocationClient?.lastLocation?.addOnSuccessListener { loc: Location? ->
            val url = if (destinationCoord != null)
                Directions.buildUrl(loc, destinationCoord)
            else
                Directions.buildUrl(loc, destination)
            apiController.get(API.DIRECTIONS, url) { response ->
                if (response != null) {
                    // val route = Directions.buildRoute(response)
                    // setMarkersAndRoute(route)
                }
            }
        }
    }

    // Bottom sheet
    private fun createBottomSheetCb(): BottomSheetBehavior.BottomSheetCallback {
        return object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(view: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> state = BottomSheetBehavior.STATE_HIDDEN
                    BottomSheetBehavior.STATE_EXPANDED -> state = BottomSheetBehavior.STATE_EXPANDED
                    BottomSheetBehavior.STATE_COLLAPSED -> state =
                        BottomSheetBehavior.STATE_COLLAPSED
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        if (state != BottomSheetBehavior.STATE_HALF_EXPANDED) {
                            sheetBehavior!!.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                        }
                    }
                    BottomSheetBehavior.STATE_SETTLING -> state = BottomSheetBehavior.STATE_SETTLING
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> state =
                        BottomSheetBehavior.STATE_HALF_EXPANDED
                }
            }

            override fun onSlide(view: View, v: Float) {}
        }
    }


}
