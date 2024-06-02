package com.meloda.kubsau.model

import com.meloda.kubsau.database.grades.Grades
import org.jetbrains.exposed.sql.ResultRow

data class Grade(
    val id: Int,
    val title: String
) {

    companion object {

        fun mapResultRow(row: ResultRow): Grade = Grade(
            id = row[Grades.id].value,
            title = row[Grades.title]
        )
    }
}
