package com.meloda.kubsau.database.specializationsdisciplines

import com.meloda.kubsau.database.disciplines.Disciplines
import com.meloda.kubsau.database.specializations.Specializations
import org.jetbrains.exposed.dao.id.IntIdTable

object SpecializationsDisciplines : IntIdTable() {
    val disciplineId = integer("disciplineId").references(Disciplines.id)
    val specializationId = integer("specializationId").references(Specializations.id)
}
