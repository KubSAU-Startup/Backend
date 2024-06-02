package com.meloda.kubsau.database.works

import com.meloda.kubsau.database.DatabaseConfig.dbQuery
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
                    student = Student.mapFromDb(row).mapToEntryStudent(StudentStatus.mapResultRow(row)),
                    group = Group.mapResultRow(row),
                    discipline = Discipline.mapResultRow(row),
                    employee = Employee.mapResultRow(row),
                    work = mapResultRow(row).mapToEntryWork(WorkType.mapResultRow(row)),
                    department = Department.mapResultRow(row)
                )
            }
    }

    override suspend fun allLatestWorksByQuery(offset: Int?, limit: Int?, query: String): List<Entry> = dbQuery {
        val q = "%$query%"

        Works
            .innerJoin(Disciplines, { Works.disciplineId }, { Disciplines.id })
            .innerJoin(Students, { Works.studentId }, { Students.id })
            .innerJoin(StudentStatuses, { Students.statusId }, { StudentStatuses.id })
            .innerJoin(WorkTypes, { Works.workTypeId }, { WorkTypes.id })
            .innerJoin(Groups, { Students.groupId }, { Groups.id })
            .innerJoin(Employees, { Works.employeeId }, { Employees.id })
            .innerJoin(Departments, { Disciplines.departmentId }, { Departments.id })
            .selectAll()
            .where {
                (Students.lastName.lowerCase() like q) or
                        (Students.firstName.lowerCase() like q) or
                        (Students.middleName.lowerCase() like q) or
                        (Groups.title.lowerCase() like q) or
                        (WorkTypes.title.lowerCase() like q) or
                        (StudentStatuses.title.lowerCase() like q) or
                        (Disciplines.title.lowerCase() like q) or
                        (Employees.lastName.lowerCase() like q) or
                        (Employees.firstName.lowerCase() like q) or
                        (Employees.middleName.lowerCase() like q) or
                        (Departments.title.lowerCase() like q) or
                        (Works.title.lowerCase() like q)
            }
            .apply {
                if (limit != null) {
                    limit(limit, (offset ?: 0).toLong())
                }
            }
            .map { row ->
                Entry(
                    student = Student.mapFromDb(row).mapToEntryStudent(StudentStatus.mapResultRow(row)),
                    group = Group.mapResultRow(row),
                    discipline = Discipline.mapResultRow(row),
                    employee = Employee.mapResultRow(row),
                    work = mapResultRow(row).mapToEntryWork(WorkType.mapResultRow(row)),
                    department = Department.mapResultRow(row)
                )
            }
    }

    override suspend fun allWorksBySearch(
        offset: Int?,
        limit: Int?,
        disciplineId: Int?,
        studentId: Int?,
        groupId: Int?,
        employeeId: Int?,
        departmentId: Int?,
        workTypeId: Int?,
        query: String?
    ): List<Entry> = dbQuery {
        val dbQuery =
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
                .orderBy(Works.registrationDate, order = SortOrder.DESC)


        studentId?.let { dbQuery.andWhere { Works.studentId eq studentId } }
        groupId?.let { dbQuery.andWhere { Students.groupId eq groupId } }
        disciplineId?.let { dbQuery.andWhere { Works.disciplineId eq disciplineId } }
        departmentId?.let { dbQuery.andWhere { Disciplines.departmentId eq departmentId } }
        workTypeId?.let { dbQuery.andWhere { Works.workTypeId eq workTypeId } }
        employeeId?.let { dbQuery.andWhere { Works.employeeId eq employeeId } }
        query?.let { "%$it%" }?.let { q ->
            dbQuery.andWhere {
                (Students.lastName.lowerCase() like q) or
                        (Students.firstName.lowerCase() like q) or
                        (Students.middleName.lowerCase() like q) or
                        (Groups.title.lowerCase() like q) or
                        (WorkTypes.title.lowerCase() like q) or
                        (StudentStatuses.title.lowerCase() like q) or
                        (Disciplines.title.lowerCase() like q) or
                        (Employees.lastName.lowerCase() like q) or
                        (Employees.firstName.lowerCase() like q) or
                        (Employees.middleName.lowerCase() like q) or
                        (Departments.title.lowerCase() like q) or
                        (Works.title.lowerCase() like q)
            }
        }

        dbQuery.map { row ->
            Entry(
                student = Student.mapFromDb(row).mapToEntryStudent(StudentStatus.mapResultRow(row)),
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
    ): Work? = dbQuery {
        Works.insert {
            it[Works.disciplineId] = disciplineId
            it[Works.studentId] = studentId
            it[Works.registrationDate] = registrationDate
            it[Works.title] = title
            it[Works.workTypeId] = workTypeId
            it[Works.employeeId] = employeeId
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

    override fun mapResultRow(row: ResultRow): Work = Work.mapFromDb(row)
}
