package com.meloda.kubsau.model

import com.meloda.kubsau.base.Filterable
import com.meloda.kubsau.database.worktypes.WorkTypes
import org.jetbrains.exposed.sql.ResultRow

data class WorkType(
    override val id: Int,
    override val title: String,
    val needTitle: Boolean
) : Filterable {

    companion object {

        fun mapResultRow(row: ResultRow): WorkType = WorkType(
            id = row[WorkTypes.id].value,
            title = row[WorkTypes.title],
            needTitle = row[WorkTypes.needTitle]
        )
    }
}
