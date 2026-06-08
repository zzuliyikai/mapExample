package com.hanshow.mapExample.util

import java.security.MessageDigest

object EncryptUtils {

    /**
     * MD5 hash of input string, returns uppercase hex string
     */
    fun md5(input: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val hashBytes = digest.digest(input.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }.uppercase()
    }
}