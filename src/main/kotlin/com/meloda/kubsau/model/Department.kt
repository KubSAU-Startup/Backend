package com.meloda.kubsau.model

data class Department(
    override val id: Int,
    override val title: String,
    val phone: String
) : Filterable
