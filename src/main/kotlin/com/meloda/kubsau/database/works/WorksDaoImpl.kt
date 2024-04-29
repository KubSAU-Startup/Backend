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
import com.meloda.kubsau.route.journal.JournalItem
import com.meloda.kubsau.route.journal.mapToJournalStudent
import com.meloda.kubsau.route.journal.mapToJournalWork
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
                (workTypeId?.let { Works.workTypeId eq workTypeId }) andIfNotNull
                (employeeId?.let { EmployeesDepartments.employeeId eq employeeId })

        Works
            .innerJoin(Disciplines)
            .innerJoin(Students)
            .innerJoin(Groups)
            .innerJoin(Employees)
            .innerJoin(Departments)
            .innerJoin(WorkTypes)
            .innerJoin(EmployeesDepartments)
            .selectAll()
            .where(query)
            .map { row ->
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
