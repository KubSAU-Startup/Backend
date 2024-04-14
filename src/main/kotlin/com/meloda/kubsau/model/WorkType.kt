package com.meloda.kubsau.model

data class WorkType(
    override val id: Int,
    override val title: String,
    val isEditable: Boolean
) : Filterable
