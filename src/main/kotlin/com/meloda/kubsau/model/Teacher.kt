package com.meloda.kubsau.model

data class Teacher(
    override val id: Int,
    val firstName: String,
    val lastName: String,
    val middleName: String,
    val departmentId: Int
) : Filterable {
    override val title: String
        get() = "$lastName $firstName $middleName"
}
