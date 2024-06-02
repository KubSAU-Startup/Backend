package com.meloda.kubsau.database.programsdisciplines

import com.meloda.kubsau.database.disciplines.Disciplines
import com.meloda.kubsau.database.programs.Programs
import com.meloda.kubsau.database.worktypes.WorkTypes
import org.jetbrains.exposed.dao.id.IntIdTable

object ProgramsDisciplines : IntIdTable() {
    val programId = integer("programId").references(Programs.id)
    val disciplineId = integer("disciplineId").references(Disciplines.id)
    val workTypeId = integer("workTypeId").references(WorkTypes.id)
}
