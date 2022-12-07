package com.safelyapp.android.view.activities

import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
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
    private lateinit var transaction: FragmentTransaction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Control y manejo de cada Fragment
        signupFragment = SignupFragment()
        loginFragment = LoginFragment()

        // Inicio del fragment Login
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, loginFragment).commit()
    }

    fun click(view: View) {
        val transaction = supportFragmentManager.beginTransaction()

        // Cambio entre fragments dependiendo si se selecciona iniciar sesion o crear una cuenta
        when(view.id){
            R.id.btn_signup_alt -> {
                transaction.replace(R.id.fragment_container, signupFragment).commit()
                //transaction.addToBackStack(null)
            }

            R.id.btn_login_alt -> {
                transaction.replace(R.id.fragment_container, loginFragment).commit()
                //transaction.addToBackStack(null)
            }
        }
    }
}