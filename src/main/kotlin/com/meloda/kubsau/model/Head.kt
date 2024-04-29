package com.meloda.kubsau.model

import com.meloda.kubsau.database.heads.Heads
import org.jetbrains.exposed.sql.ResultRow

data class Head(
    val id: Int,
    val code: String,
    val abbreviation: String,
    val title: String,
    val facultyId: Int
) {

    companion object {

        fun mapResultRow(row: ResultRow): Head = Head(
            id = row[Heads.id].value,
            code = row[Heads.code],
            abbreviation = row[Heads.abbreviation],
            title = row[Heads.title],
            facultyId = row[Heads.facultyId]
        )
    }
}
