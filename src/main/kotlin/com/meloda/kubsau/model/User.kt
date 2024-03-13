package com.meloda.kubsau.model

data class User(
    val id: Int,
    val login: String,
    // TODO: 24/02/2024, Danil Nikolaev: SECURITY, FOR FUCK's SAKE
    val password: String,
    val type: Int,
    val departmentId: Int
)
