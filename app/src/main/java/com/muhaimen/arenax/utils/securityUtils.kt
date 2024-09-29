package com.muhaimen.arenax.utils

import java.security.MessageDigest

object securityUtils {
    fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val hashedBytes = md.digest(password.toByteArray())
        return hashedBytes.joinToString("") { "%02x".format(it) }
    }
}
