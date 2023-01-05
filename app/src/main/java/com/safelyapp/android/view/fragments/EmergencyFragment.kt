package com.safelyapp.android.view.fragments

import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.lifecycleScope
import com.safelyapp.android.R
import com.safelyapp.android.databinding.FragmentEmergencyBinding
import com.safelyapp.android.databinding.FragmentListRequestBinding
import com.safelyapp.android.view.Database.DbContacts
import com.safelyapp.android.view.activities.HomeActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EmergencyFragment : Fragment() {

    // ========== General ==========
    private var _binding: FragmentEmergencyBinding? = null
    private val binding get() = _binding!!
    private val dbContacts = DbContacts()

    // ========== Elements ==========
    private lateinit var tv_count: TextView
    private lateinit var btn_cancel: Button
    private lateinit var location: Location
    private var count = 5
    private var listEmailContact: List<String> = listOf()

    // ========== Ciclo de vida ==========
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEmergencyBinding.inflate(inflater, container, false)

        // Asignacion de elementos mediante binding
        tv_count = binding.tvCount
        btn_cancel = binding.btnEmergency

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        observer()
    }

    // ========== Metodos propios ==========
    fun getLastLocation(location: Location) {
        this.location = location
    }

    private fun observer() {
        count()
        cancel()
    }

    // El usuario podra cancelar el envio de la alerta de emergencia si oprime el boton
    private fun cancel() {
        btn_cancel.setOnClickListener {
            returnParentFragment()
        }
    }

    // Conteo regresivo para enviar la ubicacion actual
    private fun count() {
        lifecycleScope.launch(Dispatchers.IO){
            while (count > 0) {
                delay(1000)
                count -= 1

                withContext(Dispatchers.Main) {
                    tv_count.text = count.toString()

                    if (count == 0) {
                        sendLocation()
                        if (isVisible)
                            returnParentFragment()
                    }
                }
            }
        }
    }

    // Envio de ubicacion del usuario a sus contactos mediante Firestore
    private suspend fun sendLocation() {
        listEmailContact = dbContacts.getListUsers(requireContext(), "contact", (activity as HomeActivity).email)
        if (listEmailContact.isNotEmpty()) {
            for (email in listEmailContact) {
                val emailWithCorrectFormat = replaceFormatEmail((activity as HomeActivity).email)
                dbContacts.addLocationRegister((activity as HomeActivity).email, email , "alert_$emailWithCorrectFormat", location)
            }
        }
    }

    // Se muestra la vista del Fragment anterior (MapsFragment)
    private fun returnParentFragment() {
        parentFragmentManager.commit {
            replace<MapsFragment>(R.id.fragment_container)
        }
        (activity as HomeActivity).nav_menu_bottom.visibility = View.VISIBLE
    }

    // Cambio de caracter especial punto (.) por guion bajo (_)
    private fun replaceFormatEmail(email: String): String {
        return email.replace(".", "_")
    }

}