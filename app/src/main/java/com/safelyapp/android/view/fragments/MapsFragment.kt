package com.safelyapp.android.view.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.safelyapp.android.R
import com.safelyapp.android.databinding.FragmentMapsBinding

class MapsFragment : Fragment(R.layout.fragment_maps), OnMapReadyCallback {

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    private lateinit var googleMap: GoogleMap

    // Forma deprecada (base):
    /*
    override fun onActivityCreate(savedInstanceState: Bundle?) {
        super.onActivityCreate(savedInstanceState)
        binding.frameGoogleMaps.onCreate(savedInstanceState)
        binding.frameGoogleMaps.onResume()
        binding.frameGoogleMaps.getMapAsync(this)
    }
    */

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)

        binding.frameGoogleMaps.onCreate(savedInstanceState)
        binding.frameGoogleMaps.onResume()
        binding.frameGoogleMaps.getMapAsync(this)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // Generacion de mapa
        generateMap()
    }

    private fun generateMap() {
        //val mapFragment = parentFragmentManager.findFragmentById(R.id.ma)
    }

    override fun onMapReady(map: GoogleMap) {
        map?.let {
            googleMap = it
        }
    }

}