package com.meloda.kubsau.common

object Constants {

    const val BACKEND_VERSION = "0.2.10"
}

val IS_IN_DOCKER = getEnvOrElse("IS_DOCKER") { "false" }.toBooleanStrict()

const val MAX_ITEMS_SIZE = 200
const val MAX_PROGRAMS = 30
const val MAX_LATEST_WORKS = 30

val LimitRange = 0..MAX_ITEMS_SIZE
val ProgramRange = 0..MAX_PROGRAMS
val LatestWorksRange = 0..MAX_LATEST_WORKS
