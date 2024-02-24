package com.meloda.kubsau.base

object Constants {

    const val BACKEND_VERSION = "0.0.4"
}

val isInDocker = System.getenv("IS_DOCKER") == "true"
