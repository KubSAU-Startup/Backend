package com.meloda.kubsau.common

object Constants {

    const val BACKEND_VERSION = "0.0.7"
}

val isInDocker = System.getenv("IS_DOCKER") == "true"
