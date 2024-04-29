package com.meloda.kubsau.database.employees

import com.meloda.kubsau.database.employeetypes.EmployeeTypes
import org.jetbrains.exposed.dao.id.IntIdTable

object Employees : IntIdTable() {
    val lastName = text("lastName")
    val firstName = text("firstName")
    val middleName = text("middleName").nullable()
    val email = text("email").nullable()
    val employeeTypeId = integer("employeeTypeId").references(EmployeeTypes.id)
}
