package com.meloda.kubsau.database.departmentfaculty

import com.meloda.kubsau.database.departments.Departments
import com.meloda.kubsau.database.faculties.Faculties
import org.jetbrains.exposed.dao.id.IntIdTable

object DepartmentsFaculties : IntIdTable() {
    val facultyId = integer("facultyId").references(Faculties.id)
    val departmentId = integer("departmentId").references(Departments.id)
}
