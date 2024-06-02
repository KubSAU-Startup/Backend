package com.meloda.kubsau.model

import com.meloda.kubsau.database.studentstatuses.StudentStatuses
import org.jetbrains.exposed.sql.ResultRow

data class StudentStatus(
    val id: Int,
    val title: String
) {

    companion object {

        fun mapFromDb(row: ResultRow): StudentStatus = StudentStatus(
            id = row[StudentStatuses.id].value,
            title = row[StudentStatuses.title]
        )
    }
}
