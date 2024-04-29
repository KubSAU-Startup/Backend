package com.meloda.kubsau.database.programs

import com.meloda.kubsau.database.directivities.Directivities
import org.jetbrains.exposed.dao.id.IntIdTable

object Programs : IntIdTable() {
    val title = text("title")
    val semester = integer("semester")
    val directivityId = integer("directivityId").references(Directivities.id)
}
