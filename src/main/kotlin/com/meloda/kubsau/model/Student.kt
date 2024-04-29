package com.meloda.kubsau.model

import com.meloda.kubsau.database.students.Students
import org.jetbrains.exposed.sql.ResultRow

data class Student(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val groupId: Int,
    val statusId: Int
) {
    val fullName: String
        get() = if (middleName == null) {
            "$firstName $lastName"
        } else {
            "$lastName $firstName $middleName"
        }

    companion object {

        fun mapResultRow(row: ResultRow): Student = Student(
            id = row[Students.id].value,
            firstName = row[Students.firstName],
            lastName = row[Students.lastName],
            middleName = row[Students.middleName],
            groupId = row[Students.groupId],
            statusId = row[Students.statusId]
        )
    }
}
