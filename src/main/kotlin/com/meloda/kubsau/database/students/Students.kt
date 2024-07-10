package com.meloda.kubsau.database.students

import com.meloda.kubsau.database.groups.Groups
import org.jetbrains.exposed.dao.id.IntIdTable

object Students : IntIdTable() {
    val lastName = text("lastName")
    val firstName = text("firstName")
    val middleName = text("middleName").nullable()
    val groupId = integer("groupId").references(Groups.id)
    val status = integer("status")
}
