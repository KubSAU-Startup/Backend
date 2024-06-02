package com.meloda.kubsau.database.groups

import com.meloda.kubsau.database.directivities.Directivities
import org.jetbrains.exposed.dao.id.IntIdTable

object Groups : IntIdTable() {
    val title = text("title")
    val directivityId = integer("directivityId").references(Directivities.id)
}
