package com.example.biometricauthshowcase.utils

import android.util.Base64
import javax.crypto.Cipher

class CryptoManager {
    data class EncryptedData(
        val encryptedData: String,
        val iv: String
    )

    fun encryptData(plaintext: String, cipher: Cipher): EncryptedData? {
        return try {
            val encryptedBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
            val iv = cipher.iv

            val encryptedBase64 = Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
            val ivBase64 = Base64.encodeToString(iv, Base64.DEFAULT)

            EncryptedData(encryptedBase64, ivBase64)

        }
        catch (e: Exception) {
            null
        }
    }


}