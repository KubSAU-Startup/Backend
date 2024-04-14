package com.meloda.kubsau.model

import com.meloda.kubsau.database.specializations.Specializations
import org.jetbrains.exposed.sql.ResultRow

data class Specialization(
    val id: Int,
    val title: String
) {

    companion object {
        fun mapResultRow(row: ResultRow): Specialization = Specialization(
            id = row[Specializations.id].value,
            title = row[Specializations.title]
        )
    }
}
