package com.meloda.kubsau.database.works

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.database.departments.Departments
import com.meloda.kubsau.database.disciplines.Disciplines
import com.meloda.kubsau.database.employees.Employees
import com.meloda.kubsau.database.employeesdepartments.EmployeesDepartments
import com.meloda.kubsau.database.groups.Groups
import com.meloda.kubsau.database.students.Students
import com.meloda.kubsau.database.worktypes.WorkTypes
import com.meloda.kubsau.model.*
import com.meloda.kubsau.route.journal.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

class WorksDaoImpl : WorksDao {

    override suspend fun allWorks(): List<Work> = dbQuery {
        Works.selectAll().map(::mapResultRow)
    }

    override suspend fun allWorksByFilters(
        disciplineId: Int?,
        studentId: Int?,
        groupId: Int?,
        employeeId: Int?,
        departmentId: Int?,
        workTypeId: Int?
    ): List<JournalItem> = dbQuery {
        val query = (studentId?.let { Works.studentId eq studentId } ?: Op.TRUE) andIfNotNull
                (groupId?.let { Students.groupId eq groupId }) andIfNotNull
                (disciplineId?.let { Works.disciplineId eq disciplineId }) andIfNotNull
                (departmentId?.let { Disciplines.departmentId eq departmentId }) andIfNotNull
                (workTypeId?.let { Works.workTypeId eq workTypeId })
                (employeeId?.let { EmployeesDepartments.employeeId eq employeeId })

        Works
            .innerJoin(Disciplines)
            .innerJoin(Students)
            .innerJoin(WorkTypes)
            .innerJoin(Groups, { Students.groupId }, { Groups.id })
            .innerJoin(Departments, { Disciplines.departmentId }, { Departments.id })
            .innerJoin(EmployeesDepartments, { Departments.id }, { EmployeesDepartments.departmentId })
            .innerJoin(Employees, { EmployeesDepartments.employeeId }, { Employees.id })
            .selectAll()
            .where { query }
            .map { row ->
//                JournalItem(
//                    student = JournalStudent(id = 8853, fullName = "Laurie Gillespie"),
//                    group = Group(id = 2392, title = "possim", directivityId = 5593),
//                    discipline = Discipline(id = 9212, title = "postulant", departmentId = 7796),
//                    employee = Employee(
//                        id = 2745,
//                        lastName = "Joanne Shaffer",
//                        firstName = "Wade Suarez",
//                        middleName = null,
//                        email = null,
//                        employeeTypeId = 4804
//                    ),
//                    work = JournalWork(
//                        id = 8028,
//                        type = WorkType(id = 1123, title = "curabitur", needTitle = false),
//                        registrationDate = 2221,
//                        title = null
//                    ),
//                    department = Department(id = 3300, title = "movet", phone = "(919) 213-1355")
//
//
//                )
                JournalItem(
                    student = Student.mapResultRow(row).mapToJournalStudent(),
                    group = Group.mapResultRow(row),
                    discipline = Discipline.mapResultRow(row),
                    employee = Employee.mapResultRow(row),
                    work = mapResultRow(row).mapToJournalWork(WorkType.mapResultRow(row)),
                    department = Department.mapResultRow(row)
                )
            }
    }

    override suspend fun allWorksByIds(workIds: List<Int>): List<Work> = dbQuery {
        Works
            .selectAll()
            .where { Works.id inList workIds }
            .map(::mapResultRow)
    }

    override suspend fun singleWork(workId: Int): Work? = dbQuery {
        Works
            .selectAll()
            .where { Works.id eq workId }
            .map(::mapResultRow)
            .singleOrNull()
    }

    override suspend fun addNewWork(
        disciplineId: Int,
        studentId: Int,
        registrationDate: Long,
        title: String?,
        workTypeId: Int
    ): Work? = dbQuery {
        Works.insert {
            it[Works.disciplineId] = disciplineId
            it[Works.studentId] = studentId
            it[Works.registrationDate] = registrationDate
            it[Works.title] = title
            it[Works.workTypeId] = workTypeId
        }.resultedValues?.singleOrNull()?.let(::mapResultRow)
    }

    override suspend fun updateWork(
        workId: Int,
        disciplineId: Int,
        studentId: Int,
        registrationDate: Long,
        title: String?,
        workTypeId: Int
    ): Boolean = dbQuery {
        Works.update(where = { Works.id eq workId }) {
            it[Works.disciplineId] = disciplineId
            it[Works.studentId] = studentId
            it[Works.registrationDate] = registrationDate
            it[Works.title] = title
            it[Works.workTypeId] = workTypeId
        } > 0
    }

    override suspend fun deleteWork(workId: Int): Boolean = dbQuery {
        Works.deleteWhere { Works.id eq workId } > 0
    }

    override suspend fun deleteWorks(workIds: List<Int>): Boolean = dbQuery {
        Works.deleteWhere { Works.id inList workIds } > 0
    }

    override fun mapResultRow(row: ResultRow): Work = Work.mapResultRow(row)
}
