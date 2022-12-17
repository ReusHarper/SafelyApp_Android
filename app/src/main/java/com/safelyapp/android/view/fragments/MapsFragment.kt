package com.safelyapp.android.view.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.UiSettings
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView
import com.safelyapp.android.R
import com.safelyapp.android.databinding.FragmentMapsBinding
import com.safelyapp.android.view.activities.HomeActivity


class MapsFragment : Fragment(R.layout.fragment_maps), OnMapReadyCallback {

    // ========== General ==========
    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!
    private var bundle: Bundle? = null
    // private lateinit var activityContainer : MapsFragmentComunicate?

    // ========== Data User ==========
    private var coordinate: LatLng = LatLng(28.043893, -16.539329)
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // ========== Elements ==========
    private lateinit var btn_emergency: Button
    private lateinit var btn_menu: Button
    private lateinit var btn_alerts: Button
    private lateinit var btn_map: Button
    private lateinit var btn_marker: Button
    private lateinit var drawer_layout: DrawerLayout
    private lateinit var nav_view: NavigationView

    // ========== Data Google ==========
    private lateinit var googleMap: GoogleMap
    private lateinit var uiSettings: UiSettings
    private var aniPosition: Boolean = false
    private var typeMap: Int = 0

    // ========== Ciclo de vida ==========
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        bundle = savedInstanceState

        // Asignacion de elementos mediante binding
        btn_emergency = binding.btnEmergency
        btn_menu = binding.btnMenu
        btn_alerts = binding.btnAlerts
        btn_map = binding.btnTypemap
        btn_marker = binding.btnMarker
        drawer_layout = binding.mapsDrawerLayout
        nav_view = binding.mapsNav

        // Obtencion de geolocalizacion actual
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        getMap(savedInstanceState)
        return binding.root
    }

    // Vinculacion de datos entre el Fragment actual con el Parent Activity (HomeActivity)
    override fun onAttach(context: Context) {
        super.onAttach(context)

        // Si se necesita recibir datos del Child Fragment:
        /*
        if (context is MapsFragmentComunicate)
            activityContainer = context
        else throw RuntimeException(
            context.toString() + " debe implementar el contexto MapsFragmentComunicate"
        )
        */
    }

    // Desvinculacion de datos entre el Fragment actual con el Parent Activity (HomeActivity)
    override fun onDetach() {
        super.onDetach()

        //activityContainer = null
    }

    override fun onResume() {
        super.onResume()

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = false
            (activity as HomeActivity).showAlert(
                "Permisos insuficientes",
                "Por favor ve a ajustes y acepta los permisos.",
                "Aceptar"
            )
            return
        }

        // Observador de acciones de usuario
        observer()
    }

    override fun onStart() {
        super.onStart()

        // Observador de acciones de usuario
        observer()
    }

    // ========== Metodos propios ==========
    // Obtencion del mapa
    fun getMap(savedInstanceState: Bundle?) {
        binding.frameGoogleMaps.onCreate(savedInstanceState)
        binding.frameGoogleMaps.onResume()
        binding.frameGoogleMaps.getMapAsync(this)
    }

    // Verificacion de carga completa del mapa
    override fun onMapReady(map: GoogleMap) {
        map.let {
            googleMap = it
        }

        // Eliminacion de configuraciones en la UI proveidas por Google Maps
        uiSettings = googleMap.uiSettings
        uiSettings.setMyLocationButtonEnabled(false)
        uiSettings.setCompassEnabled(false)

        // Observador de acciones de usuario
        observer()
    }

    // Comprobacion de los permisos
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            (activity as HomeActivity).showAlert(
                "Permisos insuficientes",
                "Para poder utilizar la aplicacion es necesario permitir la geolocalizacion. Ve a ajustes y habilita dicho permiso.",
                "Aceptar"
            )
            return
        }
        googleMap.isMyLocationEnabled = true
    }

    // Habilitacion de la localizacion actual
    private fun enableLocation() {
        // Si el mapa aun no fue renderizado, entonces se debe esperar a su realizacion
        if (!::googleMap.isInitialized) return

        // Si el usuario ha aceptado los permisos necesarios de geolocalizaccion entonces se procede a localizar su ubicacion
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            (activity as HomeActivity).requestLocationPermission()
            return
        }
        googleMap.isMyLocationEnabled = true

        // Estalecimiento de marca en la posicion actual del usuario
        getCurrentLocation()
    }

    // Obtencion de la geolocalizacion actual
    private fun getCurrentLocation()  {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Codigo en caso de que los permisos no hayan sido concedidos
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location : Location ->
            setLocation(location)

            // Establecimiento de marcador en la posicion registrada
            val currentPosition = LatLng(location.latitude, location.longitude)
            setMarker(currentPosition)
        }
    }

    // Generacion de marcador con la ubicacion actual del usuario
    private fun setMarker(currentPosition: LatLng) {

        coordinate = LatLng(currentPosition.latitude, currentPosition.longitude)
        val marker = MarkerOptions().position(coordinate).title("Posicion actual")
        googleMap.addMarker(marker)

        // Si es la 1era vez en mostrarse el mapa, se procede con realizar una animacion corta
        if (!aniPosition) {
            animationCamera(coordinate, 3000, 18f)
            aniPosition = true
        }
        else {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 18f))
        }
    }

    fun setLocation(location: Location) { lastLocation = location }

    fun getLocation() : Location { return lastLocation }

    // Observador de acciones de usuario
    private fun observer() {
        enableLocation()
        changeTypeMap()
        returnCurrentPosition()
        showMenu()
    }

    // Cambio de mapa 2D a 3D
    private fun changeTypeMap() {
        btn_map.setOnClickListener{
            if (typeMap == 0) {
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL)
                typeMap = 1
            }
            else if (typeMap == 1) {
                googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN)
                typeMap = 2
            }
            else {
                googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID)
                typeMap = 0
            }
        }
    }

    // Reubicacion de marcador
    private fun returnCurrentPosition() {
        btn_marker.setOnClickListener {
            animationCamera(LatLng(lastLocation.latitude, lastLocation.longitude), 2000, 16f)
        }
    }

    // Reubica la posicion de la camara mediante una animacion corta
    private fun animationCamera(coordinate: LatLng, duration: Int, zoom: Float) {
        googleMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(coordinate, zoom),
            duration,
            null
        )
    }

    // Despliega un menu lateral si se oprime el boton de menu
    private fun showMenu() {
        btn_menu.setOnClickListener {
            if (!drawer_layout.isDrawerOpen(Gravity.LEFT)) {
                drawer_layout.openDrawer(Gravity.LEFT)
            }
            else
                drawer_layout.closeDrawer(Gravity.RIGHT)
        }
    }

}