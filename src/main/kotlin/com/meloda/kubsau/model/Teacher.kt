package com.meloda.kubsau.model

import com.meloda.kubsau.database.teachers.Teachers
import org.jetbrains.exposed.sql.ResultRow

data class Teacher(
    override val id: Int,
    val firstName: String,
    val lastName: String,
    val middleName: String,
    val departmentId: Int
) : Filterable {
    override val title: String
        get() = "$lastName $firstName $middleName"

    companion object {
        fun mapResultRow(row: ResultRow): Teacher = Teacher(
            id = row[Teachers.id].value,
            firstName = row[Teachers.firstName],
            lastName = row[Teachers.lastName],
            middleName = row[Teachers.middleName],
            departmentId = row[Teachers.departmentId]
        )
    }
}
