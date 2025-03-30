package com.example.biometricauthshowcase.utils

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class BiometricHelper(private val activity: FragmentActivity) {

    enum class BiometricStatus {
        AVAILABLE,
        NO_HARDWARE,
        UNAVAILABLE,
        NOT_ENROLLED,
        UNKNOWN
    }

    sealed class AuthResult {
        data class Success(val cryptoObject: BiometricPrompt.CryptoObject?) : AuthResult()
        data class Error(val code: Int, val message: String) : AuthResult()
        object Failure : AuthResult()
    }

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    fun checkBiometricCapability(context: Context, allowWeakBiometrics: Boolean = false): BiometricStatus {
        val biometricManager = BiometricManager.from(context)
        val authenticators = if (allowWeakBiometrics) {
            BIOMETRIC_STRONG or BIOMETRIC_WEAK
        } else {
            BIOMETRIC_STRONG
        }
        return when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricStatus.UNAVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NOT_ENROLLED
            else -> BiometricStatus.UNKNOWN
        }
    }

    fun setupBiometricAuthentication(title: String = "Biometric Authentication",
                                     subtitle: String = "Log in using your biometric credential",
                                     description: String = "Confirm your biometric to continue",
                                     allowDeviceCredential: Boolean = true,
                                     onResult: (AuthResult) -> Unit) {
        val executor = ContextCompat.getMainExecutor(activity)

        biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onResult(AuthResult.Success(result.cryptoObject))
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onResult(AuthResult.Error(errorCode, errString.toString()))
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onResult(AuthResult.Failure)
            }

        })

        val promptBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)


        if (allowDeviceCredential) { // change to allow face and fingerprint
            promptBuilder.setAllowedAuthenticators(BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
        } else {
            promptBuilder.setNegativeButtonText("Cancel")
            promptBuilder.setAllowedAuthenticators(BIOMETRIC_STRONG)
        }

        promptInfo = promptBuilder.build()

    }

    fun authenticate() {
        biometricPrompt.authenticate(promptInfo)
    }

    fun authenticateWithCrypto(cryptoObject: BiometricPrompt.CryptoObject) {
        biometricPrompt.authenticate(promptInfo, cryptoObject)
    }

    fun generateBiometricKey(keyName: String): SecretKey? {
     try {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

         val keyGenerator = KeyGenerator.getInstance(
             KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
         )

         val keyGenParameterSpec = KeyGenParameterSpec.Builder(keyName, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
             .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
             .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
             .setUserAuthenticationRequired(true)
             .setInvalidatedByBiometricEnrollment(true)
             .build()

         keyGenerator.init(keyGenParameterSpec)
         return keyGenerator.generateKey()

     } catch (e: Exception) {
         Log.e("BiometricHelper", "Error generating key: ${e.message}")
         return null
     }
    }
    fun getCipherForEncryption(keyName: String): Cipher? {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            val secretKey = keyStore.getKey(keyName, null) as SecretKey
            val cipher = Cipher.getInstance(
                KeyProperties.KEY_ALGORITHM_AES + "/" +
                        KeyProperties.BLOCK_MODE_CBC + "/" +
                        KeyProperties.ENCRYPTION_PADDING_PKCS7
            )

            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            return cipher

        }catch (e: Exception) {
            Log.e("BiometricHelper", "Error getting cipher: ${e.message}")
            return null
        }
    }

    fun getCipherForDecryption(keyName: String, iv: ByteArray): Cipher? {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            val secretKey = keyStore.getKey(keyName, null) as SecretKey
            val cipher = Cipher.getInstance(
                KeyProperties.KEY_ALGORITHM_AES + "/" +
                        KeyProperties.BLOCK_MODE_CBC + "/" +
                        KeyProperties.ENCRYPTION_PADDING_PKCS7
            )

            val spec = javax.crypto.spec.IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            return cipher
        } catch (e: Exception) {
            Log.e("BiometricHelper", "Error getting cipher: ${e.message}")
            return null
        }
    }

}