package com.meloda.kubsau.common

fun getEnvOrNull(name: String): String? = runCatching {
    System.getenv(name)
}.fold(
    onSuccess = { env -> env },
    onFailure = { null }
)

fun getEnvOrElse(name: String, defaultValue: () -> String): String = getEnvOrNull(name) ?: defaultValue()
