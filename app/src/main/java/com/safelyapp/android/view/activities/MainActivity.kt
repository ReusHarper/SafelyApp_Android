package com.safelyapp.android.view.activities

import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.safelyapp.android.R
import com.safelyapp.android.databinding.ActivityMainBinding
import com.safelyapp.android.view.fragments.LoginFragment
import com.safelyapp.android.view.fragments.SignupFragment
import kotlin.math.log

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

        fragmentManager = supportFragmentManager
        fragmentTransaction = fragmentManager.beginTransaction()

        // Inicio del fragment Login
        fragmentTransaction.add(R.id.fragment_container, loginFragment).commit()
    }

    fun click(view: View) {
        fragmentTransaction = supportFragmentManager.beginTransaction()

        // Cambio entre fragments dependiendo si se selecciona iniciar sesion o crear una cuenta
        when(view.id){
            R.id.btn_signup_alt -> {
                fragmentTransaction.replace(R.id.fragment_container, signupFragment)
                fragmentTransaction.addToBackStack(null)
                fragmentTransaction.commit()
            }

            R.id.btn_login_alt -> {
                fragmentTransaction.replace(R.id.fragment_container, loginFragment)
                fragmentTransaction.addToBackStack(null)
                fragmentTransaction.commit()
            }
        }
    }
}