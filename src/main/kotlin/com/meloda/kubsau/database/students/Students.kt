package com.meloda.kubsau.database.students

import com.meloda.kubsau.database.groups.Groups
import org.jetbrains.exposed.dao.id.IntIdTable

object Students : IntIdTable() {
    val firstName = text("firstName")
    val lastName = text("lastName")
    val middleName = text("middleName")
    val groupId = integer("groupId").references(Groups.id)
    val status = integer("status")
}
