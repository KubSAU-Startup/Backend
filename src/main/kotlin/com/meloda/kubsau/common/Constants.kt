package com.meloda.kubsau.common

object Constants {

    const val BACKEND_VERSION = "0.1.0"
}

val isInDocker = getEnvOrElse("IS_DOCKER") { "false" }.toBooleanStrict()
