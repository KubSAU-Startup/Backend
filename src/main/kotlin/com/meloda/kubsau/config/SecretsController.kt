package com.meloda.kubsau.config

import com.meloda.kubsau.CONFIG_FOLDER
import com.meloda.kubsau.common.getEnvOrElse
import java.io.File
import java.util.*

object SecretsController {

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

    val dbName: String by lazy {
        getEnvOrElse("POSTGRES_DB") {
            properties.getProperty("POSTGRES_DB", "db")
        }
    }

    val dbUser: String by lazy {
        getEnvOrElse("POSTGRES_USER") {
            properties.getProperty("POSTGRES_USER", "user")
        }
    }

    val dbPassword: String by lazy {
        getEnvOrElse("POSTGRES_PASSWORD") {
            properties.getProperty("POSTGRES_PASSWORD", "password")
        }
    }

    fun init() {
        println("JWT_SECRET: $jwtSecret")

        if (ConfigController.usePostgreSQL) {
            println("POSTGRES_DB: $dbName")
            println("POSTGRES_USER: $dbUser")
            println("POSTGRES_PASSWORD: $dbPassword")
        }
    }
}

private data object NoSecretPropertiesException : Throwable()
