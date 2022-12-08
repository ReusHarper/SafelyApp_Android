package com.safelyapp.android.view.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.size
import androidx.fragment.app.*
import com.safelyapp.android.R
import com.safelyapp.android.databinding.ActivityMainBinding
import com.safelyapp.android.view.fragments.LoginFragment
import com.safelyapp.android.view.fragments.SignupFragment


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // ========== Fragments ==========
    private lateinit var signupFragment: SignupFragment
    private lateinit var loginFragment: LoginFragment
    private lateinit var fragmentManager: FragmentManager
    private lateinit var fragmentTransaction: FragmentTransaction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Control y manejo de cada Fragment
        signupFragment = SignupFragment()
        loginFragment = LoginFragment()

        supportFragmentManager.beginTransaction().add(R.id.fragment_container, loginFragment).commit()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }

    fun click(view: View) {
        fragmentTransaction = supportFragmentManager.beginTransaction()

        // Cambio entre fragments dependiendo si se selecciona iniciar sesion o crear una cuenta
        when(view.id){
            R.id.btn_signup_alt -> {
                supportFragmentManager.commit {
                    replace<SignupFragment>(R.id.fragment_container)
                    setReorderingAllowed(true)
                    //addToBackStack("signup")
                }

            }

            R.id.btn_login_alt -> {
                supportFragmentManager.commit {
                    replace<LoginFragment>(R.id.fragment_container)
                    setReorderingAllowed(true)
                    //addToBackStack("login")
                }
            }
        }
    }
}

// Limpieza de los fragments acumulados para evitar superposionamiento de vistas
//binding.fragmentContainer.removeAllViews()