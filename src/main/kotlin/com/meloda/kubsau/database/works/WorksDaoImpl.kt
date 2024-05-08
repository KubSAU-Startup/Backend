package com.meloda.kubsau.database.works

import com.meloda.kubsau.database.DatabaseController.dbQuery
import com.meloda.kubsau.database.departments.Departments
import com.meloda.kubsau.database.disciplines.Disciplines
import com.meloda.kubsau.database.employees.Employees
import com.meloda.kubsau.database.groups.Groups
import com.meloda.kubsau.database.students.Students
import com.meloda.kubsau.database.studentstatuses.StudentStatuses
import com.meloda.kubsau.database.worktypes.WorkTypes
import com.meloda.kubsau.model.*
import com.meloda.kubsau.route.works.Entry
import com.meloda.kubsau.route.works.mapToEntryStudent
import com.meloda.kubsau.route.works.mapToEntryWork
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

class WorksDaoImpl : WorksDao {

    override suspend fun allWorks(
        offset: Int?,
        limit: Int?
    ): List<Work> = dbQuery {
        Works
            .selectAll()
            .apply {
                if (limit != null) {
                    limit(limit, (offset ?: 0).toLong())
                }
            }
            .map(::mapResultRow)
    }

    override suspend fun allLatestWorks(offset: Int?, limit: Int?): List<Entry> = dbQuery {
        Works
            .innerJoin(Disciplines, { Works.disciplineId }, { Disciplines.id })
            .innerJoin(Students, { Works.studentId }, { Students.id })
            .innerJoin(StudentStatuses, { Students.statusId }, { StudentStatuses.id })
            .innerJoin(WorkTypes, { Works.workTypeId }, { WorkTypes.id })
            .innerJoin(Groups, { Students.groupId }, { Groups.id })
            .innerJoin(Employees, { Works.employeeId }, { Employees.id })
            .innerJoin(Departments, { Disciplines.departmentId }, { Departments.id })
            .selectAll()
            .apply {
                if (limit != null) {
                    limit(limit, (offset ?: 0).toLong())
                }
            }
            .map { row ->
                Entry(
                    student = Student.mapResultRow(row).mapToEntryStudent(StudentStatus.mapResultRow(row)),
                    group = Group.mapResultRow(row),
                    discipline = Discipline.mapResultRow(row),
                    employee = Employee.mapResultRow(row),
                    work = mapResultRow(row).mapToEntryWork(WorkType.mapResultRow(row)),
                    department = Department.mapResultRow(row)
                )
            }
    }

    override suspend fun allWorksByFilters(
        offset: Int?,
        limit: Int?,
        disciplineId: Int?,
        studentId: Int?,
        groupId: Int?,
        employeeId: Int?,
        departmentId: Int?,
        workTypeId: Int?,
    ): List<Entry> = dbQuery {
        val query =
            Works
                .innerJoin(Disciplines, { Works.disciplineId }, { Disciplines.id })
                .innerJoin(Students, { Works.studentId }, { Students.id })
                .innerJoin(StudentStatuses, { Students.statusId }, { StudentStatuses.id })
                .innerJoin(WorkTypes, { Works.workTypeId }, { WorkTypes.id })
                .innerJoin(Groups, { Students.groupId }, { Groups.id })
                .innerJoin(Employees, { Works.employeeId }, { Employees.id })
                .innerJoin(Departments, { Disciplines.departmentId }, { Departments.id })
                .selectAll()
                .apply {
                    if (limit != null) {
                        limit(limit, (offset ?: 0).toLong())
                    }
                }

        studentId?.let { query.andWhere { Works.studentId eq studentId } }
        groupId?.let { query.andWhere { Students.groupId eq groupId } }
        disciplineId?.let { query.andWhere { Works.disciplineId eq disciplineId } }
        departmentId?.let { query.andWhere { Works.departmentId eq departmentId } }
        workTypeId?.let { query.andWhere { Works.workTypeId eq workTypeId } }
        employeeId?.let { query.andWhere { Works.employeeId eq employeeId } }

        query.map { row ->
            Entry(
                student = Student.mapResultRow(row).mapToEntryStudent(StudentStatus.mapResultRow(row)),
                group = Group.mapResultRow(row),
                discipline = Discipline.mapResultRow(row),
                employee = Employee.mapResultRow(row),
                work = mapResultRow(row).mapToEntryWork(WorkType.mapResultRow(row)),
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
        workTypeId: Int,
        employeeId: Int,
        departmentId: Int
    ): Work? = dbQuery {
        Works.insert {
            it[Works.disciplineId] = disciplineId
            it[Works.studentId] = studentId
            it[Works.registrationDate] = registrationDate
            it[Works.title] = title
            it[Works.workTypeId] = workTypeId
            it[Works.employeeId] = employeeId
            it[Works.departmentId] = departmentId
        }.resultedValues?.singleOrNull()?.let(::mapResultRow)
    }

    override suspend fun updateWork(
        workId: Int,
        disciplineId: Int,
        studentId: Int,
        registrationDate: Long,
        title: String?,
        workTypeId: Int,
        employeeId: Int,
        departmentId: Int
    ): Boolean = dbQuery {
        Works.update(where = { Works.id eq workId }) {
            it[Works.disciplineId] = disciplineId
            it[Works.studentId] = studentId
            it[Works.registrationDate] = registrationDate
            it[Works.title] = title
            it[Works.workTypeId] = workTypeId
            it[Works.departmentId] = departmentId
        } > 0
    }

    override suspend fun deleteWork(workId: Int): Boolean = dbQuery {
        Works.deleteWhere { Works.id eq workId } > 0
    }

    override suspend fun deleteWorks(workIds: List<Int>): Boolean = dbQuery {
        Works.deleteWhere { Works.id inList workIds } > 0
    }

    override fun mapResultRow(row: ResultRow): Work = Work.mapFromDb(row)
}
