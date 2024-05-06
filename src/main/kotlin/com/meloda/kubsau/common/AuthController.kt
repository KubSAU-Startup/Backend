package com.meloda.kubsau.common

import com.meloda.kubsau.CONFIG_FOLDER
import java.io.File
import java.util.*

object AuthController {

    private val properties: Properties by lazy {
        val secretPropertiesFile = File("$CONFIG_FOLDER/secret.properties")
        if (!secretPropertiesFile.exists()) {
            throw NoSecretPropertiesException
        }

        Properties().apply { load(secretPropertiesFile.inputStream()) }
    }

    val jwtSecret: String by lazy {
        properties.getProperty("JWT_SECRET")
    }

    fun init() {
        println("JWT_SECRET: $jwtSecret")
    }
}

private data object NoSecretPropertiesException : Throwable()
