package com.meloda.kubsau.common

object Constants {

    const val BACKEND_VERSION = "0.2.6"
}

val IS_IN_DOCKER = getEnvOrElse("IS_DOCKER") { "false" }.toBooleanStrict()

const val MAX_ITEMS_SIZE = 200

val LimitRange = 0..MAX_ITEMS_SIZE
