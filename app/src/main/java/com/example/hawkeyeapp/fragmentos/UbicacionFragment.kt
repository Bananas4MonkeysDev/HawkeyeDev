package com.example.hawkeyeapp.fragmentos

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.hawkeyeapp.Model.Estado
import com.example.hawkeyeapp.R
import com.example.hawkeyeapp.databinding.FragmentUbicacionBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.sql.Timestamp

class UbicacionFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentUbicacionBinding? = null
    private val binding get() = _binding!!

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var locationRequest: LocationRequest? = null
    private var sosTimer: CountDownTimer? = null
    private var isSosButtonEnabled = true

    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private var currentViajeId: String? = null  // ID del viaje actual

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUbicacionBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        currentViajeId = arguments?.getString("viajeId")  // Recuperar la ID del viaje

        setupVideoView(currentViajeId!!)
        setupButtonListeners(currentViajeId!!)

        return binding.root
    }

    private fun setupVideoView(idVia: String) {
        val videoPath = "android.resource://" + requireActivity().packageName + "/" + R.raw.countdown
        binding.videoCountdown.setVideoURI(Uri.parse(videoPath))
        binding.videoCountdown.setOnCompletionListener {
            binding.videoCountdown.visibility = View.GONE
            binding.btnCancelar.visibility = View.GONE
            binding.btnSOS.visibility = View.VISIBLE
            updateEstado("Emergencia", idVia)  // Estado de emergencia después de que el contador termine
        }
    }

    private fun setupButtonListeners(idVia:String) {
        binding.btnSOS.setOnClickListener {
            if (isSosButtonEnabled) {
                isSosButtonEnabled = false
                binding.videoCountdown.visibility = View.VISIBLE
                binding.btnCancelar.visibility = View.VISIBLE
                binding.btnSOS.visibility = View.GONE
                binding.videoCountdown.start()
                updateEstado("Riesgo",idVia)  // Estado de riesgo cuando se presiona SOS
                sendSOS()
            }
        }

        binding.btnCancelar.setOnClickListener {
            cancelSOS()
            updateEstado("Normal",idVia)  // Volver al estado normal al cancelar
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        enableLocation()
        try {
            // Cargar el estilo del mapa desde el archivo JSON
            val success = mMap.setMapStyle(

                MapStyleOptions.loadRawResourceStyle(
                    requireContext(), R.raw.map_style
                )
            )
            if (!success) {
                Log.e("MainActivity", "Fallo al cargar el estilo del mapa.")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error al cargar el estilo del mapa: ${e.message}")
        }
    }

    private fun enableLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            mMap.isMyLocationEnabled = true
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    updateLocationInFirebase(location.latitude, location.longitude)
                }
            }
        }
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest!!, locationCallback, null)
        }
    }

    private fun sendSOS() {
        sosTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Handle countdown logic and UI
            }

            override fun onFinish() {
                isSosButtonEnabled = true
                showSnackbar("SOS Signal Sent and Location Updated")
            }
        }.start()
    }

    private fun cancelSOS() {
        sosTimer?.cancel()
        sosTimer = null
        isSosButtonEnabled = true
        binding.videoCountdown.visibility = View.GONE
        binding.btnCancelar.visibility = View.GONE
        binding.btnSOS.visibility = View.VISIBLE
        showSnackbar("SOS Cancelled")
    }

    private fun updateLocationInFirebase(latitude: Double, longitude: Double) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            // Obtiene las referencias para cada dato de ubicación por separado
            val ref = FirebaseDatabase.getInstance().getReference("Pasajeros/${it.uid}/location")
            ref.child("latitude").setValue(latitude)
                .addOnSuccessListener {
                    Log.d("UbicacionFragment", "Latitude Updated in Firebase")
                }
                .addOnFailureListener {
                    Log.e("UbicacionFragment", "Failed to Update Latitude in Firebase: ${it.message}")
                }
            ref.child("longitude").setValue(longitude)
                .addOnSuccessListener {
                    Log.d("UbicacionFragment", "Longitude Updated in Firebase")
                }
                .addOnFailureListener {
                    Log.e("UbicacionFragment", "Failed to Update Longitude in Firebase: ${it.message}")
                }
        } ?: Log.e("UbicacionFragment", "No authenticated user found.")
    }

    private fun updateEstado(estado: String,idVia: String) {
        val timestamp = System.currentTimeMillis()
        val estadoObjeto = Estado("",idVia,estado, Timestamp(timestamp))
        currentViajeId?.let { viajeId ->
            val refEstado = FirebaseDatabase.getInstance().getReference("Estados").push()
            estadoObjeto.id = refEstado.key ?: ""
            refEstado.setValue(estadoObjeto).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Actualizar el viaje con el nuevo estado
                    FirebaseDatabase.getInstance().getReference("Viajes/$viajeId/estados")
                        .child(estadoObjeto.id).setValue(true)
                    Log.d("UbicacionFragment", "Estado $estado registrado correctamente.")
                } else {
                    Log.e("UbicacionFragment", "Error al registrar el estado: ${task.exception?.message}")
                }
            }
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableLocation()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        _binding = null
    }

    companion object {
        fun newInstance(viajeId: String): UbicacionFragment {
            val fragment = UbicacionFragment()
            val args = Bundle()
            args.putString("viajeId", viajeId)
            fragment.arguments = args
            return fragment
        }
    }
}
