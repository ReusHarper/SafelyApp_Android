package com.safelyapp.android.view.activities

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.safelyapp.android.R
import com.safelyapp.android.databinding.ActivityHomeBinding
import com.safelyapp.android.view.fragments.*

enum class ProviderType {
    BASIC,
    CORREO,
    GOOGLE,
    FACEBOOK
}

class HomeActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE_LOCATION = 0
    }

    // ========== General ==========
    private lateinit var binding: ActivityHomeBinding
    internal val db = FirebaseFirestore.getInstance()

    // ========== Elements ==========
    internal lateinit var nav_menu_bottom: BottomNavigationView
    internal lateinit var nav_notify_button: Button
    private lateinit var drawer_layout: DrawerLayout
    private lateinit var nav_menu_side: NavigationView
    internal lateinit var email: String
    internal lateinit var providerType: String
    private lateinit var locationCurrent: Location

    // ========== Fragments ==========
    private lateinit var mapsFragment: MapsFragment
    private lateinit var groupsFragment: GroupsFragment
    private lateinit var devicesFragment: DevicesFragment

    private lateinit var medicalFragment: MedicalHistoryFragment
    private lateinit var codeSOSFragment: CodeSosFragment
    private lateinit var accountFragment: AccountFragment

    private lateinit var listContactFragment: ListContactFragment
    private lateinit var listAddFragment: ListAddFragment
    private lateinit var listRequestFragment: ListRequestFragment


    private lateinit var fragmentManager: FragmentManager
    private lateinit var fragmentTransaction: FragmentTransaction

    // ========== Ciclo de vida ==========
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.extras
        email = bundle?.getString("email").toString()
        providerType = bundle?.getString("provider").toString()
        setup(email ?: "", providerType ?: "")

        // Control y manejo de cada Fragment
        mapsFragment = MapsFragment()
        groupsFragment = GroupsFragment()
        devicesFragment = DevicesFragment()
        medicalFragment = MedicalHistoryFragment()
        codeSOSFragment = CodeSosFragment()
        accountFragment = AccountFragment()
        listContactFragment = ListContactFragment()
        listAddFragment = ListAddFragment()
        listRequestFragment = ListRequestFragment()

        // Control y manejo de los elementos
        nav_menu_bottom = binding.navMenu
        drawer_layout = binding.homeDrawerLayout
        nav_menu_side = binding.navMenuSide

        // Observacion de bottom menu
        bottomMenu()

        // Agregacion de vista inicial
        supportFragmentManager.commit {
            add<MapsFragment>(R.id.fragment_container)
            setReorderingAllowed(true)
            addToBackStack("maps")

            //getLocationCurrent()
        }
    }

    override fun onStart() {
        super.onStart()

        // Observacion de bottom menu
        bottomMenu()
    }

    override fun onBackPressed() {
        super.onBackPressed()

        //if (supportFragmentManager.backStackEntryCount == 0)
        val fragmentCurrent = supportFragmentManager.findFragmentById(R.id.fragment_container)

        if (fragmentCurrent is MapsFragment)
            nav_menu_bottom.visibility = View.VISIBLE
        else if (supportFragmentManager.backStackEntryCount == 0)
            this@HomeActivity.finish()
    }

    // ========== Metodos propios ==========
    // Comprobacion de correo y metodo de ingreso
    private fun setup(email: String, providerType: String) {
        //binding.emailTextView.text = email
        //binding.providerTextView.text = providerType

        // Se almacenan los datos de inicio de sesion
        val preferencias = getSharedPreferences(getString(R.string.file_preferencia), Context.MODE_PRIVATE).edit()
        preferencias.putString("email", email)
        preferencias.putString("proovedor", providerType)
        preferencias.apply()
    }

    // Funcionalidad del menu principal
    private fun bottomMenu() {
        fragmentTransaction = supportFragmentManager.beginTransaction()

        // Cambio entre fragments dependiendo de la seleccion desde el bar menu
        nav_menu_bottom.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.item_fragment_map -> {
                    //replaceFragment(mapsFragment)
                    supportFragmentManager.commit {
                        replace<MapsFragment>(R.id.fragment_container)
                    }
                }
                R.id.item_fragment_group -> {
                    supportFragmentManager.commit {
                        replace<GroupsFragment>(R.id.fragment_container)
                    }
                }
                R.id.item_fragment_devices -> {
                    supportFragmentManager.commit {
                        replace<DevicesFragment>(R.id.fragment_container)
                    }
                }
                else -> {
                    supportFragmentManager.commit {
                        replace<MapsFragment>(R.id.fragment_container)
                    }
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

    // Sustitucion del fragment por los del menu lateral
    internal fun menuFragment() {
        fragmentTransaction = supportFragmentManager.beginTransaction()

        // Comprobacion de desplazamiento de menu lateral
        sideScrolling()

        // Cambio entre fragments dependiendo de la seleccion desde el bar menu
        nav_menu_side.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.item_nav_home -> {
                    supportFragmentManager.commit {
                        replace<MapsFragment>(R.id.fragment_container)
                        setReorderingAllowed(true)
                    }
                    nav_menu_bottom.visibility = View.VISIBLE
                }
                R.id.item_nav_medical -> {
                    supportFragmentManager.commit {
                        replace<MedicalHistoryFragment>(R.id.fragment_container)
                        setReorderingAllowed(true)
                        addToBackStack("medical_history")
                    }
                    nav_menu_bottom.visibility = View.GONE
                }
                R.id.item_nav_profile -> {
                    supportFragmentManager.commit {
                        replace<AccountFragment>(R.id.fragment_container)
                        setReorderingAllowed(true)
                        addToBackStack("account")
                    }
                    nav_menu_bottom.visibility = View.GONE
                }
                R.id.item_nav_logout -> {
                    // Se limpian las credencias de sesion
                    val preferencias = getSharedPreferences(getString(R.string.file_preferencia), Context.MODE_PRIVATE).edit()
                    preferencias.clear()
                    preferencias.apply()

                    // Cierre de sesión por Firebase y retorno a vista inicial
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }

                else -> {
                    showAlert(
                        "Error de sistema",
                        "Ha ocurrido un problema con la vista solicitada, por favor reintente de nuevo.",
                        "Aceptar"
                    )
                }
            }
            // Una vez se seleccione una opcion, el menu se debera ocultar
            sideScrolling()
            true
        }
    }

    // Desplazamiento de menu lateral
    internal fun sideScrolling() {
        if (!drawer_layout.isDrawerOpen(Gravity.LEFT)) {
            drawer_layout.openDrawer(Gravity.LEFT)
        }
        else
            drawer_layout.closeDrawer(Gravity.LEFT)
    }

    internal fun notifications() {
        fragmentTransaction = supportFragmentManager.beginTransaction()

        supportFragmentManager.commit {
            replace<NotificationsFragment>(R.id.fragment_container)
            setReorderingAllowed(true)
            addToBackStack("notifications")
        }
        nav_menu_bottom.visibility = View.GONE
    }

    // Comprobacion de permisos
    internal fun isLocationPermissionGranted() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    // Solicitud de permiso de geolocalizacion
    internal fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            showAlert(
                "Permisos insuficientes",
                "Para poder utilizar la aplicacion es necesario permitir la geolocalizacion. Ve a ajustes y habilita dicho permiso.",
                "Aceptar"
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION
            )
        }
    }

    // Envio de alertas personalizadas
    internal fun showAlert(title: String, message: String, buttonAction: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(buttonAction, null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}