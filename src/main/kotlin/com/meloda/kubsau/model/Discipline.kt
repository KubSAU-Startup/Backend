package com.meloda.kubsau.model

import com.meloda.kubsau.database.disciplines.Disciplines
import org.jetbrains.exposed.sql.ResultRow

data class Discipline(
    override val id: Int,
    override val title: String,
    val workTypeId: Int
) : Filterable {

    companion object {
        fun mapResultRow(row: ResultRow): Discipline = Discipline(
            id = row[Disciplines.id].value,
            title = row[Disciplines.title],
            workTypeId = row[Disciplines.workTypeId]
        )
    }
}
