package com.meloda.kubsau.model

import com.meloda.kubsau.database.programs.Programs
import org.jetbrains.exposed.sql.ResultRow

data class Program(
    val id: Int,
    val semester: Int,
    val directivityId: Int
) {

    companion object {

        fun mapResultRow(row: ResultRow): Program = Program(
            id = row[Programs.id].value,
            semester = row[Programs.semester],
            directivityId = row[Programs.directivityId]
        )
    }
}
