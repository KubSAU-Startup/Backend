package com.meloda.kubsau.database.disciplines

import com.meloda.kubsau.database.worktypes.WorkTypes
import org.jetbrains.exposed.dao.id.IntIdTable

object Disciplines : IntIdTable() {
    val title = text("title")
    val workTypeId = integer("workTypeId").references(WorkTypes.id)
}
