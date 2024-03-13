package com.meloda.kubsau.model

data class Discipline(
    override val id: Int,
    override val title: String
): Filterable
