package com.nopoint.midpoint.fragments

import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import com.nopoint.midpoint.R
import com.nopoint.midpoint.map.DirectionsUtils
import com.nopoint.midpoint.map.MapsFactory
import com.nopoint.midpoint.map.models.FullRoute
import com.nopoint.midpoint.networking.API
import com.nopoint.midpoint.networking.APIController
import com.nopoint.midpoint.networking.ServiceVolley
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.bottom_sheet.view.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.nopoint.midpoint.map.MeetingUtils
import com.nopoint.midpoint.map.MeetingsSingleton
import com.nopoint.midpoint.models.CurrentUser
import com.nopoint.midpoint.models.LocalUser
import kotlinx.android.synthetic.main.fragment_map.view.*
import java.lang.NullPointerException


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
    private lateinit var sheetFragment: MeetingFragment
    private lateinit var currentLocation: Location
    private lateinit var localUser: LocalUser

    private lateinit var fabMapIntent: FloatingActionButton

    lateinit var buttonOpenMaps: Button

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
        localUser = CurrentUser.getLocalUser(activity!!)!!
        childFragmentManager.beginTransaction().replace(R.id.google_map, mapFragment!!).commit()
        sheetFragment = childFragmentManager.findFragmentById(R.id.fragment) as MeetingFragment
        sheetBehavior = BottomSheetBehavior.from(view.bottom_sheet)
        sheetBehavior!!.bottomSheetCallback = createBottomSheetCb()

        view.btn.setOnClickListener {
            val active = MeetingsSingleton.getActiveMeeting()
            sheetFragment.arrived(active!!)
        }
        fabMapIntent = view.findViewById(R.id.fab_map_intent)

        return view
    }

    fun getDirectionsToAbsoluteMidpoint(midpointURL: String, clearPrevious: Boolean) {
        apiController.get(API.DIRECTIONS, midpointURL) { response ->
            if (response != null) {
                if (clearPrevious) clearMarkersAndRoute()
                if (state == BottomSheetBehavior.STATE_EXPANDED) {
                    sheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
                }
                val route = DirectionsUtils.buildRoute(response)
                setMarkersAndRoute(route)

                if (route.endLat != null && route.endLng != null) {
                    fabMapIntent.visibility = View.VISIBLE
                    fabMapIntent.setOnClickListener {
                        openMapsIntent(route.endLat, route.endLng)
                    }
                }
            }
        }
    }

    private fun openMapsIntent(lat: Double, lng: Double) {
        val gmIntentUri = Uri.parse("google.navigation:q=$lat,$lng&mode=w")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        try {
            if (mapIntent.resolveActivity(activity!!.packageManager) != null) {
                startActivity(mapIntent)
            } else {
                Toast.makeText(activity, "Couldn't open map", Toast.LENGTH_SHORT).show()
            }
        } catch (e: NullPointerException) {
            Log.e("MAP INTENT", "$e")
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(activity, R.raw.map_style))
        if (!requestingLocationUpdates) {
            mMap.isMyLocationEnabled = true
            // Starting location over Helsinki
            val cam = CameraUpdateFactory.newLatLngZoom(LatLng(60.2, 24.7385084), 8.0f)
            mMap.moveCamera(cam)
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
            locationCallback = createLocationCallback()
            startLocationUpdates()
        }
    }

    override fun onResume() {
        super.onResume()
        if (requestingLocationUpdates) startLocationUpdates()
    }

    private fun createBottomSheetCb(): BottomSheetBehavior.BottomSheetCallback {
        return object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(view: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> state = BottomSheetBehavior.STATE_HIDDEN
                    BottomSheetBehavior.STATE_EXPANDED -> state = BottomSheetBehavior.STATE_EXPANDED
                    BottomSheetBehavior.STATE_COLLAPSED -> state =
                        BottomSheetBehavior.STATE_COLLAPSED
                    BottomSheetBehavior.STATE_DRAGGING -> state = BottomSheetBehavior.STATE_DRAGGING
/*                    Buggy AF
                        if (state != BottomSheetBehavior.STATE_HALF_EXPANDED) {
                            sheetBehavior!!.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                    }*/
                    BottomSheetBehavior.STATE_SETTLING -> state = BottomSheetBehavior.STATE_SETTLING
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> state =
                        BottomSheetBehavior.STATE_HALF_EXPANDED
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
                        Log.d("MAP", "lat: ${location.latitude}, lng: ${location.longitude}")
                        val cam = CameraUpdateFactory.newLatLngZoom(loc, 15.0f)
                        sheetFragment.currentLocation =
                            LatLng(location.latitude, location.longitude)
                        currentLocation = location
                        mMap.animateCamera(cam)
                    } else {
                        val active = MeetingsSingleton.getActiveMeeting()
                        if ( active != null){
                            if (MeetingUtils.reachedLocation(active, location)) {
                                sheetFragment.arrived(active)
                            }
                        }
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
}
