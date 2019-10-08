package com.nopoint.midpoint.helpers

import android.util.Base64
import java.security.MessageDigest
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class EncryptionHelper private constructor() {
    private var encryptionKey: String? = null

    @Throws(Exception::class)
    fun getSecreteKey(secretKey: String): SecretKey {
        val md = MessageDigest.getInstance("SHA-1")
        val digestOfPassword = md.digest(secretKey.toByteArray(charset("UTF-8")))
        val keyBytes = digestOfPassword.copyOf(24)
        return SecretKeySpec(keyBytes, "AES")
    }

    fun encryptMsg(): String {
        return Base64.encodeToString(encryptionKey!!.toByteArray(), Base64.DEFAULT)
    }

    fun encryptionString(encryptionKey: String): EncryptionHelper? {
        this.encryptionKey = encryptionKey
        return encryptionHelper
    }

    fun getDecryptionString(encryptedText: String): String {
        return String(Base64.decode(encryptedText.toByteArray(), Base64.DEFAULT))
    }

    companion object {

        private val encryptionHelper = EncryptionHelper()

        val instance: EncryptionHelper
            get() {
                return encryptionHelper
            }
    }
}