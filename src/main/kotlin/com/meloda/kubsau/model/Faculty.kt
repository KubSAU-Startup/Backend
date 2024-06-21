package com.meloda.kubsau.model

import com.meloda.kubsau.database.faculties.Faculties
import org.jetbrains.exposed.sql.ResultRow

data class Faculty(
    val id: Int,
    val title: String
) {

    companion object {

        fun mapFromDb(row: ResultRow): Faculty = Faculty(
            id = row[Faculties.id].value,
            title = row[Faculties.title]
        )
    }
}
