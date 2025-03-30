package com.example.biometricauthshowcase

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.biometricauthshowcase.utils.BiometricHelper
import com.example.biometricauthshowcase.utils.CryptoManager
import androidx.biometric.BiometricPrompt

class MainActivity : AppCompatActivity() {

    private lateinit var checkAuthButton: Button
    private lateinit var simpleAuthButton: Button
    private lateinit var encryptButton: Button
    private lateinit var dataEditText: EditText
    private lateinit var encryptedDataTextView: TextView


    private lateinit var biometricHelper: BiometricHelper
    private lateinit var cryptoManager: CryptoManager

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
        cryptoManager = CryptoManager()
        checkBiometricAvailability()

        checkAuthButton = findViewById(R.id.checkAuthButton)
        checkAuthButton.setOnClickListener {
            checkBiometricAvailability()
        }

        simpleAuthButton = findViewById(R.id.simpleAuthButton)
        simpleAuthButton.setOnClickListener {
            performAuthentication()
        }


        dataEditText = findViewById(R.id.dataEditText)
        encryptedDataTextView = findViewById(R.id.encryptedDataTextView)
        encryptButton = findViewById(R.id.encryptButton)

        encryptButton.setOnClickListener {
            encryptData()
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
        biometricHelper.authenticate()
    }

    private fun encryptData() {
        val dataToEncrypt = dataEditText.text.toString()

        if (dataToEncrypt.isEmpty()) {
            Toast.makeText(this, "Please enter data to encrypt", Toast.LENGTH_SHORT).show()
            return
        }

        biometricHelper.generateBiometricKey("biometric_demo_key")
        val cipher = biometricHelper.getCipherForEncryption("biometric_demo_key")

        biometricHelper.setupBiometricAuthentication(
            title = "Encrypt Data",
            subtitle = "Authentication required to encrypt data",
            allowDeviceCredential = false,
            onResult = { result ->
                when (result) {
                    is BiometricHelper.AuthResult.Success -> {
                        result.cryptoObject?.cipher?.let { cipher ->
                            // Encrypt data after authentication
                            val encryptedData = cryptoManager.encryptData(dataToEncrypt, cipher)
                            encryptedDataTextView.text = "Encrypted data: ${encryptedData?.encryptedData?.take(20)}....\n\nIV: ${encryptedData?.iv?.take(20)}..."
                            Toast.makeText(this, "Data encrypted successfully!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    is BiometricHelper.AuthResult.Error -> {
                        Toast.makeText(this, "Authentication error: ${result.message}", Toast.LENGTH_SHORT).show()
                    }
                    is BiometricHelper.AuthResult.Failure -> {
                        Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )

        biometricHelper.authenticateWithCrypto(BiometricPrompt.CryptoObject(cipher))
    }

}