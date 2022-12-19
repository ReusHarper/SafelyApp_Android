package com.safelyapp.android.view.activities

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.safelyapp.android.R
import com.safelyapp.android.databinding.ActivityHomeBinding
import com.safelyapp.android.view.fragments.*
import kotlin.system.exitProcess


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

    private lateinit var binding: ActivityHomeBinding

    // ========== Elements ==========
    private lateinit var nav_menu_bottom: BottomNavigationView
    private lateinit var drawer_layout: DrawerLayout
    private lateinit var nav_menu_side: NavigationView

    // ========== Fragments ==========
    private lateinit var mapsFragment: MapsFragment
    private lateinit var groupsFragment: GroupsFragment
    private lateinit var devicesFragment: DevicesFragment

    private lateinit var medicalFragment: MedicalHistoryFragment
    private lateinit var codeSOSFragment: CodeSosFragment
    private lateinit var accountFragment: AccountFragment

    private lateinit var fragmentManager: FragmentManager
    private lateinit var fragmentTransaction: FragmentTransaction

    // ========== Ciclo de vida ==========
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.extras
        val email = bundle?.getString("email")
        val providerType = bundle?.getString("provider")
        setup(email ?: "", providerType ?: "")

        // Control y manejo de cada Fragment
        mapsFragment = MapsFragment()
        groupsFragment = GroupsFragment()
        devicesFragment = DevicesFragment()
        medicalFragment = MedicalHistoryFragment()
        codeSOSFragment = CodeSosFragment()
        accountFragment = AccountFragment()


        // Control y manejo de los elementos
        nav_menu_bottom = binding.navMenu
        drawer_layout = binding.homeDrawerLayout
        nav_menu_side = binding.navMenuSide

        // Limpieza del stack de fragments
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        // Observacion de bottom menu
        bottomMenu()

        // Agregacion de vista inicial
        supportFragmentManager.commit {
            add<MapsFragment>(R.id.fragment_container)
            setReorderingAllowed(true)
            addToBackStack("maps")
        }
    }

    override fun onStart() {
        super.onStart()

        // Observacion de bottom menu
        bottomMenu()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onBackPressed() {
        super.onBackPressed()

        //if (supportFragmentManager.backStackEntryCount == 0)
        val fragmentCurrent = supportFragmentManager.findFragmentById(R.id.fragment_container)

        if (fragmentCurrent is MapsFragment) {
            nav_menu_bottom.visibility = View.VISIBLE
            Toast.makeText(this, "maps", Toast.LENGTH_SHORT).show()
        } else if (supportFragmentManager.backStackEntryCount > 0)
            Toast.makeText(this, "quedan mas", Toast.LENGTH_SHORT).show()
        else {
            this@HomeActivity.finish()
            exitProcess(0)
        }
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
                R.id.item_fragment_map -> replaceFragment(mapsFragment)
                R.id.item_fragment_group -> replaceFragment(groupsFragment)
                R.id.item_fragment_devices -> replaceFragment(devicesFragment)

                else -> {
                    showAlert(
                        "Error de sistema",
                        "Ha ocurrido un problema con la vista actual, por favor reintente de nuevo.",
                        "Aceptar"
                    )
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

    // Sustitucion del fragment por los del menu
    internal fun menuFragment() {
        fragmentTransaction = supportFragmentManager.beginTransaction()

        // Comprobacion de desplazamiento de menu lateral
        sideScrolling()

        // Cambio entre fragments dependiendo de la seleccion desde el bar menu
        nav_menu_side.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.item_nav_home -> {
                    replaceFragment(mapsFragment)
                    nav_menu_bottom.visibility = View.VISIBLE
                }
                R.id.item_nav_medical -> {
                    //replaceFragment(medicalFragment)

                    supportFragmentManager.commit {
                        replace<MedicalHistoryFragment>(R.id.fragment_container)
                        setReorderingAllowed(true)
                        addToBackStack("medical_history")
                    }
                    nav_menu_bottom.visibility = View.GONE
                }
                R.id.item_nav_code -> {
                    //replaceFragment(codeSOSFragment)

                    supportFragmentManager.commit {
                        replace<CodeSosFragment>(R.id.fragment_container)
                        setReorderingAllowed(true)
                        addToBackStack("code_sos")
                    }
                    nav_menu_bottom.visibility = View.GONE
                }
                R.id.item_nav_profile -> {
                    //replaceFragment(accountFragment)
                    supportFragmentManager.commit {
                        replace<AccountFragment>(R.id.fragment_container)
                        setReorderingAllowed(true)
                        addToBackStack("account")
                    }

                    nav_menu_bottom.visibility = View.GONE
                    //addToBackStack("signup")
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
            sideScrolling()
            true
        }

    }

    // Desplazamiento de menu lateral
    private fun sideScrolling() {
        if (!drawer_layout.isDrawerOpen(Gravity.LEFT)) {
            drawer_layout.openDrawer(Gravity.LEFT)
        }
        else
            drawer_layout.closeDrawer(Gravity.LEFT)
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