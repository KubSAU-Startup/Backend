package com.meloda.kubsau.model

data class Group(
    override val id: Int,
    override val title: String,
    val majorId: Int
): Filterable
