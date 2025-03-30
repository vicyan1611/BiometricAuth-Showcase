package com.example.biometricauthshowcase

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.biometricauthshowcase.utils.BiometricHelper

class MainActivity : AppCompatActivity() {

    private lateinit var checkAuthButton: Button
    private lateinit var simpleAuthButton: Button
    private lateinit var biometricHelper: BiometricHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        biometricHelper = BiometricHelper(this)
        checkBiometricAvailability()
        biometricHelper.setupBiometricAuthentication(onResult = {result -> when (result) {
            is BiometricHelper.AuthResult.Success -> {
                Toast.makeText(this, "Authenticated successfully!", Toast.LENGTH_SHORT).show()
            }
            is BiometricHelper.AuthResult.Error -> {
                Toast.makeText(this, "Authentication error: ${result.message}", Toast.LENGTH_SHORT).show()
            }
            is BiometricHelper.AuthResult.Failure -> {
                Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
            }
        }})

        checkAuthButton = findViewById(R.id.checkAuthButton)
        checkAuthButton.setOnClickListener {
            checkBiometricAvailability()
        }

        simpleAuthButton = findViewById(R.id.simpleAuthButton)
        simpleAuthButton.setOnClickListener {
            performAuthentication()
        }

    }
    private fun checkBiometricAvailability() {
        val status = biometricHelper.checkBiometricCapability(this)
        val statusMessage = when (status) {
            BiometricHelper.BiometricStatus.AVAILABLE -> "Biometric authentication is available"
            BiometricHelper.BiometricStatus.NO_HARDWARE -> "Biometric authentication is not available on this device"
            BiometricHelper.BiometricStatus.UNAVAILABLE -> "Biometric authentication is currently unavailable"
            BiometricHelper.BiometricStatus.NOT_ENROLLED -> "Biometric authentication is not enrolled"
            BiometricHelper.BiometricStatus.UNKNOWN -> "Biometric authentication status is unknown"
        }

        Toast.makeText(this, statusMessage, Toast.LENGTH_SHORT).show()
    }

    private fun performAuthentication() {
        biometricHelper.authenticate()
    }

}