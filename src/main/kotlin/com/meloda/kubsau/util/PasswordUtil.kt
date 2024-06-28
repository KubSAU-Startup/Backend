package com.meloda.kubsau.util

import org.mindrot.jbcrypt.BCrypt

object PasswordUtil {

    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    fun checkPassword(plaintext: String, hashed: String): Boolean {
        return runCatching {
            BCrypt.checkpw(plaintext, hashed)
        }.fold(
            onSuccess = { it },
            onFailure = { false }
        )
    }
}
