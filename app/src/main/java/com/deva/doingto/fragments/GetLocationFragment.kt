package com.deva.doingto.fragments

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import com.deva.doingto.R
import com.deva.doingto.databinding.FragmentGetLocationBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class GetLocationFragment : DialogFragment(), OnMapReadyCallback {
    private lateinit var binding: FragmentGetLocationBinding
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    //private lateinit var latitude: TextView
    //private lateinit var longitude: TextView
    private var locationListener: LocationUpdateListener? = null
    private var selectedLocation: LatLng? = null
    private var initialLatitude: Double? = null
    private var initialLongitude: Double? = null

    companion object {
        const val TAG = "GetLocationFragment"
        const val LOCATION_PERMISSION_REQUEST_CODE = 100

        fun newInstance(latitude: Double?, longitude: Double?): GetLocationFragment {
            val fragment = GetLocationFragment()
            val args = Bundle()
            if (latitude != null && longitude != null) {
                args.putDouble("latitude", latitude)
                args.putDouble("longitude", longitude)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGetLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        // val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        // mapFragment.getMapAsync(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        //latitude = view.findViewById(R.id.latitudeText)
        //longitude = view.findViewById(R.id.longitudeText)

        arguments?.let {
            if (it.containsKey("latitude") && it.containsKey("longitude")) {
                initialLatitude = it.getDouble("latitude")
                initialLongitude = it.getDouble("longitude")
            }
        }

        val button = view.findViewById<Button>(R.id.confirm_location_button)
        button.setOnClickListener {
            //getLocation()
            selectedLocation?.let {
                locationListener?.onLocationReceived(it.latitude.toString(), it.longitude.toString())
                dismiss()
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.setOnMapClickListener { latLng ->
            googleMap.clear()
            googleMap.addMarker(MarkerOptions().position(latLng).title("Selected Location"))
            selectedLocation = latLng
        }

        // getLocation()
        if (initialLatitude != null && initialLongitude != null) {
            val initialLocation = LatLng(initialLatitude!!, initialLongitude!!)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 15f))
            googleMap.addMarker(MarkerOptions().position(initialLocation).title("Previous Location"))
        } else {
            getLocation()
        }
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        val location = fusedLocationProviderClient.lastLocation
        location.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val currentLocation = task.result
                if (currentLocation != null) {
                    // val textlatitude = "Latitude: " + currentLocation.latitude.toString()
                    // val textlongitude = "Longitude: " + currentLocation.longitude.toString()
                    // latitude.text = textlatitude
                    // longitude.text = textlongitude
                    // locationListener?.onLocationReceived(textlatitude, textlongitude)
                    val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                } else {
                    // latitude.text = "Unable to get location"
                    // longitude.text = "Unable to get location"
                }
            } else {
                // latitude.text = "Location task unsuccessful"
                // longitude.text = "Location task unsuccessful"
            }
        }
    }

    fun setLocationListener(listener: LocationUpdateListener) {
        this.locationListener = listener
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    interface LocationUpdateListener {
        fun onLocationReceived(latitude: String, longitude: String)
    }
}
