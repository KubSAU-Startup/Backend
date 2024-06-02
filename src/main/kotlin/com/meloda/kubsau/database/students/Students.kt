package com.meloda.kubsau.database.students

import com.meloda.kubsau.database.groups.Groups
import com.meloda.kubsau.database.studentstatuses.StudentStatuses
import org.jetbrains.exposed.dao.id.IntIdTable

object Students : IntIdTable() {
    val lastName = text("lastName")
    val firstName = text("firstName")
    val middleName = text("middleName").nullable()
    val groupId = integer("groupId").references(Groups.id)
    val statusId = integer("statusId").references(StudentStatuses.id)
}
