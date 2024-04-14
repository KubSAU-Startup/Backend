package com.meloda.kubsau.model

import com.meloda.kubsau.database.programs.Programs
import com.meloda.kubsau.database.programs.Programs.semester
import org.jetbrains.exposed.sql.ResultRow

data class Program(
    val id: Int,
    val title: String,
    val semester: Int
) {

    companion object {
        fun mapResultRow(row: ResultRow): Program = Program(
            id = row[Programs.id].value,
            title = row[Programs.title],
            semester = row[semester]
        )
    }
}
