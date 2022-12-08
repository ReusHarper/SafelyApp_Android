package com.safelyapp.android.view.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.safelyapp.android.databinding.ActivityHomeBinding

private lateinit var binding: ActivityHomeBinding

enum class ProviderType {
    BASIC
}

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.extras
        val email = bundle?.getString("email")
        val providerType = bundle?.getString("provider")
        setup(email ?: "error", providerType ?: "error")
    }

    override fun onStart() {
        super.onStart()
    }

    private fun setup(email: String, providerType: String) {
        binding.emailTextView.text = email
        binding.providerTextView.text = providerType
    }

}