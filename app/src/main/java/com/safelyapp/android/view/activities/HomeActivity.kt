package com.safelyapp.android.view.activities

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.safelyapp.android.R
import com.safelyapp.android.databinding.ActivityHomeBinding
import com.safelyapp.android.view.fragments.*

enum class ProviderType {
    BASIC
}

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    // ========== Elements ==========
    private lateinit var navegation: BottomNavigationView

    // ========== Fragments ==========
    private lateinit var mapsFragment: MapsFragment
    private lateinit var groupsFragment: GroupsFragment
    private lateinit var devicesFragment: DevicesFragment

    private lateinit var fragmentManager: FragmentManager
    private lateinit var fragmentTransaction: FragmentTransaction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // val bundle = intent.extras
        // val email = bundle?.getString("email")
        // val providerType = bundle?.getString("provider")
        // setup(email ?: "error", providerType ?: "error")

        // Control y manejo de cada Fragment
        mapsFragment = MapsFragment()
        groupsFragment = GroupsFragment()
        devicesFragment = DevicesFragment()

        // Control y manejo de bottom bar menu
        navegation = binding.navMenu

        // Observacion de bottom menu
        bottomMenu()

        // Agregacion de vista inicial
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, mapsFragment).commit()
    }

    override fun onStart() {
        super.onStart()

        // Observacion de bottom menu
        bottomMenu()
    }

    override fun onResume() {
        super.onResume()
    }

    /*private fun setup(email: String, providerType: String) {
        binding.emailTextView.text = email
        binding.providerTextView.text = providerType

        // Cierre de sesion por Firebase
        binding.btnLogout.setOnClickListener{
            FirebaseAuth.getInstance().signOut()
            onBackPressed()
        }
    }*/

    private fun bottomMenu() {
        fragmentTransaction = supportFragmentManager.beginTransaction()

        // Cambio entre fragments dependiendo de la seleccion desde el bar menu
        navegation.setOnItemSelectedListener{
            when(it.itemId) {
                R.id.item_fragment_map -> replaceFragment(mapsFragment)
                R.id.item_fragment_group -> replaceFragment(groupsFragment)
                R.id.item_fragment_devices -> replaceFragment(devicesFragment)

                else -> {
                    showAlert("Error de sistema",
                        "Ha ocurrido un problema con la vista actual, por favor reintente de nuevo.",
                        "Aceptar")
                }
            }
            true
        }
    }

    // Sustitucion del fragment actual por el solicitado por el usuario
    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        var fragmentTransaction = fragmentManager.beginTransaction()

        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }

    private fun showAlert(title: String, message: String, buttonAction: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(buttonAction, null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

}