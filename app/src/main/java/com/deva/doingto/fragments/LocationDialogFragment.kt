package com.deva.doingto.fragments

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.deva.doingto.R
import com.deva.doingto.databinding.FragmentLocationDialogBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class LocationDialogFragment : DialogFragment(), OnMapReadyCallback {
    private lateinit var binding: FragmentLocationDialogBinding
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var selectedLatLng: LatLng? = null

    companion object {
        const val TAG = "LocationDialogFragment"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLocationDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        val confirmButton: Button = view.findViewById(R.id.confirm_location_button)
        confirmButton.setOnClickListener {
            returnSelectedLocation()
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getCurrentLocation()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true

        // Set a map click listener to place a marker and get the coordinates
        mMap.setOnMapClickListener { latLng ->
            // Clear previous markers
            mMap.clear()

            // Add a new marker
            mMap.addMarker(MarkerOptions().position(latLng).title("Selected Location"))

            // Save the selected coordinates
            selectedLatLng = latLng
        }
    }

    private fun getCurrentLocation() {
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

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                selectedLatLng = LatLng(it.latitude, it.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng!!, 19f))
                mMap.addMarker(MarkerOptions().position(selectedLatLng!!).title("Current Location"))
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to get current location", Toast.LENGTH_SHORT).show()
        }
    }

    private fun returnSelectedLocation() {
        selectedLatLng?.let {
            val result = Bundle().apply {
                putDouble("LATITUDE", it.latitude)
                putDouble("LONGITUDE", it.longitude)
            }
            Log.d(TAG, "Preparing to send Latitude: ${it.latitude}, Longitude: ${it.longitude}")
            parentFragmentManager.setFragmentResult("REQUEST_KEY", result)
            Log.d(TAG, "Result set: Latitude: ${it.latitude}, Longitude: ${it.longitude}")
            dismiss()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getCurrentLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}