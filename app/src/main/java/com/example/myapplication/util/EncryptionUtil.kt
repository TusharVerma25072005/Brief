// Create file: app/src/main/java/com/example/myapplication/util/EncryptionUtil.kt
package com.example.myapplication.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object EncryptionUtil {
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val KEY_ALIAS = "BriefyEncryptionKey"
    private const val IV_LENGTH = 12
    private const val TAG_LENGTH = 128
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"

    // Email patterns to detect and encrypt
    val sensitivePatterns = mapOf(
        "email" to Regex("""[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}"""),
        "phone" to Regex("""(\+\d{1,3}[\s-])?\(?\d{3}\)?[\s.-]?\d{3}[\s.-]?\d{4}"""),
        "address" to Regex("""(\d+)\s+([A-Za-z]+\s*)+,\s*([A-Za-z]+\s*)+,\s*([A-Za-z]{2})\s+(\d{5})"""),
        "creditcard" to Regex("""\b(?:\d{4}[-\s]?){3}\d{4}\b""")
    )

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            return keyGenerator.generateKey()
        }

        return (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
    }

    fun encrypt(data: String): Pair<String, String> {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())

        val encryptedBytes = cipher.doFinal(data.toByteArray())
        val iv = cipher.iv

        return Pair(
            Base64.encodeToString(iv, Base64.DEFAULT),
            Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        )
    }

    fun decrypt(iv: String, encryptedData: String): String {
        val ivBytes = Base64.decode(iv, Base64.DEFAULT)
        val encryptedBytes = Base64.decode(encryptedData, Base64.DEFAULT)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(TAG_LENGTH, ivBytes)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), spec)

        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes)
    }

    // Process text to encrypt sensitive data and replace with placeholders
    fun processText(text: String): Pair<String, Map<String, Pair<String, String>>> {
        var processedText = text
        val encryptedData = mutableMapOf<String, Pair<String, String>>()
        var counter = 1

        for ((type, pattern) in sensitivePatterns) {
            val matches = pattern.findAll(text)
            for (match in matches) {
                val placeholder = "[[${type}_$counter]]"
                val encryptedPair = encrypt(match.value)

                encryptedData[placeholder] = encryptedPair
                processedText = processedText.replace(match.value, placeholder)
                counter++
            }
        }

        return Pair(processedText, encryptedData)
    }

    // Restore original data from summary containing placeholders
    fun restoreText(text: String, encryptedData: Map<String, Pair<String, String>>): String {
        var restoredText = text

        for ((placeholder, encryptedPair) in encryptedData) {
            val original = decrypt(encryptedPair.first, encryptedPair.second)
            restoredText = restoredText.replace(placeholder, original)
        }

        return restoredText
    }
}