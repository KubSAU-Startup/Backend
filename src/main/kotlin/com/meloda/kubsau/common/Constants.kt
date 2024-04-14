package com.meloda.kubsau.common

object Constants {

    const val BACKEND_VERSION = "0.0.8"
}

val isInDocker = System.getenv("IS_DOCKER") == "true"
