package com.meloda.kubsau.route.works

import com.meloda.kubsau.model.respondSuccess
import com.meloda.kubsau.common.*
import com.meloda.kubsau.database.departments.DepartmentsDao
import com.meloda.kubsau.database.disciplines.DisciplinesDao
import com.meloda.kubsau.database.employees.EmployeeDao
import com.meloda.kubsau.database.groups.GroupsDao
import com.meloda.kubsau.database.students.StudentsDao
import com.meloda.kubsau.database.works.WorksDao
import com.meloda.kubsau.database.worktypes.WorkTypesDao
import com.meloda.kubsau.model.ContentNotFoundException
import com.meloda.kubsau.model.UnknownException
import com.meloda.kubsau.model.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.worksRoutes() {
    authenticate {
        route("/works") {
            getWorks()
            getWorkById()
            addWork()
            editWork()
            deleteWorkById()
            deleteWorksByIds()

            latestWorksRoutes()
        }
    }
}

private data class WorksResponse(
    val works: List<Work>
)

private data class FullWorksResponse(
    val works: List<Work>,
    val disciplines: List<Discipline>,
    val students: List<Student>,
    val workTypes: List<WorkType>,
    val departments: List<Department>,
    val employees: List<Employee>
)

private fun Route.getWorks() {
    val worksDao by inject<WorksDao>()
    val disciplinesDao by inject<DisciplinesDao>()
    val studentsDao by inject<StudentsDao>()
    val workTypesDao by inject<WorkTypesDao>()
    val departmentsDao by inject<DepartmentsDao>()
    val employeeDao by inject<EmployeeDao>()

    get {
        val parameters = call.request.queryParameters

        val workIds = parameters.getIntList(
            key = "workIds",
            defaultValue = emptyList(),
            maxSize = MAX_ITEMS_SIZE
        )

        val offset = parameters.getInt("offset")
        val limit = parameters.getInt(key = "limit", range = LimitRange)
        val extended = parameters.getBoolean("extended", false)

        val works = if (workIds.isEmpty()) {
            worksDao.allWorks(offset, limit ?: MAX_ITEMS_SIZE)
        } else {
            worksDao.allWorksByIds(workIds)
        }

        if (!extended) {
            respondSuccess { WorksResponse(works = works) }
        } else {
            val disciplineIds = works.map(Work::disciplineId)
            val disciplines = disciplinesDao.allDisciplinesByIds(disciplineIds)

            val studentIds = works.map(Work::studentId)
            val students = studentsDao.allStudentsByIds(studentIds)

            val workTypeIds = works.map(Work::workTypeId)
            val workTypes = workTypesDao.allWorkTypesByIds(workTypeIds)

            val departmentIds = disciplines.map(Discipline::departmentId)
            val departments = departmentsDao.allDepartmentsByIds(departmentIds)

            val employeeIds = works.map(Work::employeeId)
            val employees = employeeDao.allEmployeesByIds(employeeIds)

            respondSuccess {
                FullWorksResponse(
                    works = works,
                    disciplines = disciplines,
                    students = students,
                    workTypes = workTypes,
                    departments = departments,
                    employees = employees
                )
            }
        }
    }
}

private data class WorkResponse(
    val work: Work
)

private data class FullWorkResponse(
    val work: Work,
    val discipline: Discipline,
    val student: Student,
    val workType: WorkType,
    val department: Department,
    val employee: Employee,
)

private fun Route.getWorkById() {
    val worksDao by inject<WorksDao>()
    val disciplinesDao by inject<DisciplinesDao>()
    val studentsDao by inject<StudentsDao>()
    val workTypesDao by inject<WorkTypesDao>()
    val departmentsDao by inject<DepartmentsDao>()
    val employeeDao by inject<EmployeeDao>()

    get("{id}") {
        val workId = call.parameters.getIntOrThrow("id")
        val extended = call.request.queryParameters.getBoolean("extended", false)

        val work = worksDao.singleWork(workId) ?: throw ContentNotFoundException

        if (!extended) {
            respondSuccess { WorkResponse(work = work) }
        } else {
            val discipline = disciplinesDao.singleDiscipline(work.disciplineId) ?: throw ContentNotFoundException
            val student = studentsDao.singleStudent(work.studentId)
            val workType = workTypesDao.singleWorkType(work.workTypeId)
            val department = departmentsDao.singleDepartment(discipline.departmentId)
            val employee = employeeDao.singleEmployee(work.employeeId)

            if (student == null || workType == null || employee == null || department == null) {
                throw ContentNotFoundException
            }

            respondSuccess {
                FullWorkResponse(
                    work = work,
                    discipline = discipline,
                    student = student,
                    workType = workType,
                    department = department,
                    employee = employee
                )
            }
        }
    }
}

private fun Route.addWork() {
    val worksDao by inject<WorksDao>()

    post {
        val parameters = call.receiveParameters()

        val disciplineId = parameters.getIntOrThrow("disciplineId")
        val studentId = parameters.getIntOrThrow("studentId")
        val title = parameters.getString("title")
        val workTypeId = parameters.getIntOrThrow("workTypeId")
        val employeeId = parameters.getIntOrThrow("employeeId")

        val created = worksDao.addNewWork(
            disciplineId = disciplineId,
            studentId = studentId,
            registrationDate = System.currentTimeMillis(),
            title = title,
            workTypeId = workTypeId,
            employeeId = employeeId
        )

        if (created != null) {
            respondSuccess { created }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.editWork() {
    val worksDao by inject<WorksDao>()

    patch("{id}") {
        val workId = call.parameters.getIntOrThrow("id")
        val currentWork = worksDao.singleWork(workId) ?: throw ContentNotFoundException

        val parameters = call.receiveParameters()

        val disciplineId = parameters.getInt("disciplineId")
        val studentId = parameters.getInt("studentId")
        val registrationDate = parameters.getLong("registrationDate")
        val title = parameters.getString("title")
        val workTypeId = parameters.getInt("workTypeId")
        val employeeId = parameters.getInt("employeeId")

        worksDao.updateWork(
            workId = workId,
            disciplineId = disciplineId ?: currentWork.disciplineId,
            studentId = studentId ?: currentWork.studentId,
            registrationDate = registrationDate ?: currentWork.registrationDate,
            title = if ("title" in parameters) title else currentWork.title,
            workTypeId = workTypeId ?: currentWork.workTypeId,
            employeeId = employeeId ?: currentWork.employeeId
        ).let { success ->
            if (success) {
                respondSuccess { 1 }
            } else {
                throw UnknownException
            }
        }
    }
}

private fun Route.deleteWorkById() {
    val worksDao by inject<WorksDao>()

    delete("{id}") {
        val workId = call.parameters.getIntOrThrow("id")
        worksDao.singleWork(workId) ?: throw ContentNotFoundException

        if (worksDao.deleteWork(workId)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.deleteWorksByIds() {
    val worksDao by inject<WorksDao>()

    delete {
        val workIds = call.request.queryParameters.getIntListOrThrow(
            key = "workIds",
            requiredNotEmpty = true
        )

        val currentWorks = worksDao.allWorksByIds(workIds)
        if (currentWorks.isEmpty()) {
            throw ContentNotFoundException
        }

        if (worksDao.deleteWorks(workIds)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.latestWorksRoutes() {
    route("/latest") {
        latestWorksFiltersRoutes()
        getLatestWorks()
    }
}

private fun Route.latestWorksFiltersRoutes() {
    route("/filters") {
        getWorkTypesFilters()
        getDisciplinesFilters()
        getEmployeesFilters()
        getGroupsFilters()
        getDepartmentsFilters()
    }
}

private fun Route.getWorkTypesFilters() {
    val workTypesDao by inject<WorkTypesDao>()

    get("/worktypes") {
        val workTypesFilters = workTypesDao.allWorkTypesAsFilters()

        respondSuccess { workTypesFilters }
    }
}

private fun Route.getDisciplinesFilters() {
    val disciplinesDao by inject<DisciplinesDao>()

    get("/disciplines") {
        val disciplinesFilters = disciplinesDao.allDisciplinesAsFilters()

        respondSuccess { disciplinesFilters }
    }
}

private fun Route.getEmployeesFilters() {
    val employeeDao by inject<EmployeeDao>()

    get("/employees") {
        val employeeFilters = employeeDao.allTeachers().map { employee ->
            EntryFilter(
                id = employee.id,
                title = employee.fullName
            )
        }

        respondSuccess { employeeFilters }
    }
}

private fun Route.getGroupsFilters() {
    val groupsDao by inject<GroupsDao>()

    get("/groups") {
        val groupsFilters = groupsDao.allGroupsAsFilters()

        respondSuccess { groupsFilters }
    }
}

private fun Route.getDepartmentsFilters() {
    val departmentsDao by inject<DepartmentsDao>()

    get("/departments") {
        val departmentsFilters = departmentsDao.allDepartmentsAsFilters()

        respondSuccess { departmentsFilters }
    }
}

private fun Route.getLatestWorks() {
    val worksDao by inject<WorksDao>()

    get {
        val parameters = call.request.queryParameters

        val offset = parameters.getInt("offset")
        val limit = parameters.getInt(key = "limit", range = LatestWorksRange)

        val workTypeId = parameters.getInt("workTypeId")
        val disciplineId = parameters.getInt("disciplineId")
        val employeeId = parameters.getInt("employeeId")
        val departmentId = parameters.getInt("departmentId")
        val groupId = parameters.getInt("groupId")
        val studentId = parameters.getInt("studentId")
        val query = parameters.getString(
            key = "query",
            trim = true,
        )?.lowercase()

        val entries = worksDao.allWorksBySearch(
            offset = offset,
            limit = limit ?: MAX_LATEST_WORKS,
            disciplineId = disciplineId,
            studentId = studentId,
            groupId = groupId,
            employeeId = employeeId,
            departmentId = departmentId,
            workTypeId = workTypeId,
            query = query
        )

        respondSuccess {
            LatestWorksResponse(
                count = entries.size,
                offset = offset ?: 0,
                entries = entries
            )
        }
    }
}

private data class LatestWorksResponse(
    val count: Int,
    val offset: Int,
    val entries: List<Entry>
)

fun Student.mapToEntryStudent(status: StudentStatus): EntryStudent =
    EntryStudent(
        id = id,
        fullName = fullName,
        status = status
    )

fun Work.mapToEntryWork(workType: WorkType): EntryWork =
    EntryWork(
        id = id,
        type = workType,
        registrationDate = registrationDate,
        title = title,
        employeeId = employeeId
    )

data class EntryFilter(
    val id: Int,
    val title: String
)

data class Entry(
    val student: EntryStudent,
    val group: Group,
    val discipline: Discipline,
    val employee: Employee,
    val work: EntryWork,
    val department: Department
)

data class EntryStudent(
    val id: Int,
    val fullName: String,
    val status: StudentStatus
)

data class EntryWork(
    val id: Int,
    val type: WorkType,
    val registrationDate: Long,
    val title: String?,
    val employeeId: Int
)
