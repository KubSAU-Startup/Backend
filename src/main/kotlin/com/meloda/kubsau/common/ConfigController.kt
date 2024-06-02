package com.meloda.kubsau.common

import com.meloda.kubsau.CONFIG_FOLDER
import java.io.File
import java.util.*

object ConfigController {

    private val properties: Properties? by lazy {
        val configPropertiesFile = File("$CONFIG_FOLDER/config.properties")
        if (configPropertiesFile.exists()) {
            Properties().apply { load(configPropertiesFile.inputStream()) }
        } else {
            null
        }
    }

    val usePostgreSQL: Boolean by lazy {
        val defaultValue = "false"
        getEnvOrElse("USE_POSTGRES") {
            properties?.getProperty("USE_POSTGRES", defaultValue) ?: defaultValue
        }.toBooleanStrict()
    }

    val dbUrl: String by lazy {
        val defaultValue = "localhost:5432"
        getEnvOrElse("DB_URL") {
            properties?.getProperty("DB_URL", defaultValue) ?: defaultValue
        }
    }

    fun init() {
        println("USE_POSTGRES: $usePostgreSQL")

        if (usePostgreSQL) {
            println("DB_URL: $dbUrl")
        }
    }
}
