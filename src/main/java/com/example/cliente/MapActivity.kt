package com.example.cliente

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import com.google.android.material.snackbar.Snackbar
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import java.util.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var database: FirebaseDatabase
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var policeMarkers = HashMap<String, Marker>()
    private lateinit var sendLocationButton: Button
    private lateinit var cancelButton: Button
    private lateinit var imgVerde: ImageView
    private lateinit var imgNaranja: ImageView
    private lateinit var imgRojo: ImageView
    private lateinit var videoCountdown: VideoView

    private var estadoActual: Estado = Estado.VERDE
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private var locationRequest: LocationRequest? = null
    private var sendingLocation = false
    private var locationReference = FirebaseDatabase.getInstance().getReference("clientes/${UUID.randomUUID()}") // Unique client ID
    private val policeLocationReference = FirebaseDatabase.getInstance().getReference("policia")
    private var isAppRunning = false
    private var sosTimer: CountDownTimer? = null
    private var isSosButtonEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        database = FirebaseDatabase.getInstance()

        // Inicializar FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Inicializar el mapa
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Inicializar las vistas
        imgVerde = findViewById(R.id.imgVerde)
        imgNaranja = findViewById(R.id.imgNaranja)
        imgRojo = findViewById(R.id.imgRojo)
        sendLocationButton = findViewById(R.id.btnSOS)
        cancelButton = findViewById(R.id.btnCancelar)
        videoCountdown = findViewById(R.id.videoCountdown)

        // Configurar el video del contador
        val videoPath = "android.resource://" + packageName + "/" + R.raw.countdown
        videoCountdown.setVideoURI(Uri.parse(videoPath))
        videoCountdown.setOnCompletionListener {
            videoCountdown.visibility = View.GONE
        }

        sendLocationButton.setOnClickListener {
            if (isSosButtonEnabled && !sendingLocation) {
                sendSOS()
            }
        }

        cancelButton.setOnClickListener {
            if (sendingLocation) {
                authenticateToCancelSOS()
                showSnackbar("Cancelando envio de ubicación")
            } else {
                cancelSOS()
                showSnackbar("Estado seguro")
            }
        }

        // Configurar el LocationCallback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (sendingLocation && isAppRunning) {
                    locationResult.lastLocation?.let { location ->
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                        sendLocation(location.latitude, location.longitude)
                    }
                }
            }
        }

        // Escuchar la ubicación del policía y actualizar el marcador del cliente
        listenPoliceLocation()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        try {
            val success = mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this, R.raw.map_style
                )
            )
            if (!success) {
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mMap.isMyLocationEnabled = true
    }

    private fun getLocationPermission() {
        if (::mMap.isInitialized && ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun startLocationUpdates() {
        locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationRequest?.let {
                fusedLocationClient.requestLocationUpdates(it, locationCallback, null)
            }
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun startSosTimer() {
        sosTimer?.cancel() // Cancelar el temporizador anterior si existe

        sendingLocation = false // Reiniciar el estado de envío

        sosTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                videoCountdown.start() // Iniciar el video de countdown
            }

            override fun onFinish() {
                if (!sendingLocation && isAppRunning) {
                    sendingLocation = true // Marcar que se está enviando la ubicación
                    startLocationUpdates() // Comenzar a actualizar la ubicación
                    enableSosButton(true) // Habilitar el botón SOS nuevamente al completar el envío
                    videoCountdown.visibility = View.GONE // Ocultar el video de countdown

                    showSnackbar("Enviando ubicación...")
                    cambiarEstado(Estado.ROJO) // Cambiar visualmente a estado ROJO
                }
            }
        }.start()
    }

    private fun enableSosButton(enable: Boolean) {
        isSosButtonEnabled = enable
        sendLocationButton.isEnabled = enable
    }

    private fun stopSosTimer() {
        sosTimer?.cancel()
    }

    private fun sendSOS() {
        if (isSosButtonEnabled) {
            isSosButtonEnabled = false // Deshabilitar el botón SOS
            startSosTimer() // Iniciar el temporizador después de mostrar el Snackbar
            cancelButton.visibility = View.VISIBLE // Mostrar el botón "Cancelar"
            sendLocationButton.visibility = View.GONE // Ocultar el botón SOS
            videoCountdown.visibility = View.VISIBLE // Mostrar el video de countdown

            showSnackbar("Estado de Alerta")
            cambiarEstado(Estado.NARANJA) // Cambiar visualmente a estado NARANJA
        }
    }

    private fun cancelSOS() {
        sosTimer?.cancel() // Cancelar el temporizador si está en marcha
        sendingLocation = false // Marcar que no se está enviando la ubicación
        deleteLocation() // Eliminar la ubicación si ya se ha enviado

        showSnackbar("Ubicación eliminada")
        enableSosButton(true)
        cancelButton.visibility = View.GONE
        sendLocationButton.visibility = View.VISIBLE
        videoCountdown.visibility = View.GONE

        cambiarEstado(Estado.VERDE) // Volver visualmente al estado VERDE
    }


    private fun showSnackbar(message: String) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
        val snackbarView = snackbar.view
        val params = snackbarView.layoutParams as ViewGroup.LayoutParams
        if (params is FrameLayout.LayoutParams) {
            params.gravity = Gravity.TOP
        }
        snackbarView.layoutParams = params
        snackbar.show()
    }


    private fun sendLocation(latitude: Double, longitude: Double) {
        val locationData = mapOf("latitude" to latitude, "longitude" to longitude)
        locationReference.setValue(locationData)
            .addOnFailureListener {
                // Manejo de error si es necesario
            }
    }

    private fun deleteLocation() {
        locationReference.removeValue()
            .addOnFailureListener {
                // Manejo de error si es necesario
            }
    }

    private fun listenPoliceLocation() {
        policeLocationReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val policeId = dataSnapshot.key ?: return
                val location = dataSnapshot.getValue(PoliceLocation::class.java) ?: return

                val latLng = LatLng(location.latitude, location.longitude)
                val marker = mMap.addMarker(
                    MarkerOptions().position(latLng).title("Policía")
                )
                policeMarkers[policeId] = marker!!
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val policeId = dataSnapshot.key ?: return
                val location = dataSnapshot.getValue(PoliceLocation::class.java) ?: return

                val marker = policeMarkers[policeId]
                marker?.position = LatLng(location.latitude, location.longitude)
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                val policeId = dataSnapshot.key ?: return
                val marker = policeMarkers.remove(policeId)
                marker?.remove()
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // No se necesita hacer nada en este caso
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Manejo de error si es necesario
            }
        })
    }

    private fun updatePoliceLocationOnClient(dataSnapshot: DataSnapshot) {
        val policeLocation = dataSnapshot.getValue(PoliceLocation::class.java)
        policeLocation?.let {
            val location = LatLng(policeLocation.latitude, policeLocation.longitude)
            var marker = policeMarkers[dataSnapshot.key]

            if (marker == null) {
                val iconBitmap = BitmapFactory.decodeResource(resources, R.drawable.icon)
                val scaledBitmap = Bitmap.createScaledBitmap(iconBitmap, 100, 100, false)
                val icon = BitmapDescriptorFactory.fromBitmap(scaledBitmap)

                marker = mMap.addMarker(
                    MarkerOptions()
                        .position(location)
                        .icon(icon)
                        .title("Policía")
                )
                policeMarkers[dataSnapshot.key!!] = marker!!
            } else {
                marker.position = location
                marker.isVisible = true
            }
        }
    }


    private fun removePoliceLocationOnClient(dataSnapshot: DataSnapshot) {
        val key = dataSnapshot.key
        val marker = policeMarkers[key]
        marker?.remove()
        policeMarkers.remove(key)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                mMap.isMyLocationEnabled = true
            }
        }
    }

    private fun authenticateToCancelSOS() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val executor = ContextCompat.getMainExecutor(this)
                val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        showSnackbar("Authentication error: $errString")
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        cancelSOS()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        showSnackbar("Authentication failed")
                    }
                })

                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Authentication required")
                    .setSubtitle("Use your fingerprint to cancel the SOS")
                    .setNegativeButtonText("Cancel")
                    .build()

                biometricPrompt.authenticate(promptInfo)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                showSnackbar("No biometric features available on this device.")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                showSnackbar("Biometric features are currently unavailable.")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                showSnackbar("No biometric credentials registered.")
        }
    }

    override fun onStart() {
        super.onStart()
        isAppRunning = true
    }

    override fun onStop() {
        super.onStop()
        isAppRunning = false
        enableSosButton(true)
    }

    override fun onResume() {
        super.onResume()
        isAppRunning = true
    }

    private fun cambiarEstado(nuevoEstado: Estado) {
        imgVerde.alpha = if (nuevoEstado == Estado.VERDE) 1.0f else 0.3f
        imgNaranja.alpha = if (nuevoEstado == Estado.NARANJA) 1.0f else 0.3f
        imgRojo.alpha = if (nuevoEstado == Estado.ROJO) 1.0f else 0.3f

        estadoActual = nuevoEstado
    }

    enum class Estado {
        VERDE, NARANJA, ROJO
    }
}