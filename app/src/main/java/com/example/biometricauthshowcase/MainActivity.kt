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
        simpleAuthButton = findViewById(R.id.simpleAuthButton)
        simpleAuthButton.setOnClickListener {
            checkBiometricAvailability()
        }
        biometricHelper = BiometricHelper()
        checkBiometricAvailability()
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
}