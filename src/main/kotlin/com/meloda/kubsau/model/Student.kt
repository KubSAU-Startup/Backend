package com.meloda.kubsau.model

import com.meloda.kubsau.database.students.Students
import org.jetbrains.exposed.sql.ResultRow

data class Student(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val groupId: Int,
    val status: Int
) {
    val fullName: String
        get() = if (middleName == null) {
            "$firstName $lastName"
        } else {
            "$lastName $firstName $middleName"
        }

    companion object {
        const val STATUS_LEARNING = 1
        const val STATUS_SABBATICAL = 2
        const val STATUS_EXPELLED = 3

        fun mapFromDb(row: ResultRow): Student = Student(
            id = row[Students.id].value,
            firstName = row[Students.firstName],
            lastName = row[Students.lastName],
            middleName = row[Students.middleName],
            groupId = row[Students.groupId],
            status = row[Students.status]
        )
    }
}
