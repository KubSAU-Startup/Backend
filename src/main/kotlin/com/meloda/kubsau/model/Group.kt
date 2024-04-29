package com.meloda.kubsau.model

import com.meloda.kubsau.database.groups.Groups
import org.jetbrains.exposed.sql.ResultRow

data class Group(
    override val id: Int,
    override val title: String,
    val directivityId: Int
) : Filterable {

    companion object {

        fun mapResultRow(row: ResultRow): Group = Group(
            id = row[Groups.id].value,
            title = row[Groups.title],
            directivityId = row[Groups.directivityId]
        )
    }
}
