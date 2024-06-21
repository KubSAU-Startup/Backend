package com.meloda.kubsau.controller

import com.meloda.kubsau.common.*
import com.meloda.kubsau.database.departmentfaculty.DepartmentsFacultiesDao
import com.meloda.kubsau.database.departments.DepartmentDao
import com.meloda.kubsau.database.disciplines.DisciplineDao
import com.meloda.kubsau.database.employees.EmployeeDao
import com.meloda.kubsau.database.groups.GroupDao
import com.meloda.kubsau.database.students.StudentDao
import com.meloda.kubsau.database.works.WorkDao
import com.meloda.kubsau.database.worktypes.WorkTypeDao
import com.meloda.kubsau.model.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

class WorkController {
    context(Route)
    private fun routes() {
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
        val workDao by inject<WorkDao>()
        val disciplineDao by inject<DisciplineDao>()
        val studentDao by inject<StudentDao>()
        val workTypeDao by inject<WorkTypeDao>()
        val departmentDao by inject<DepartmentDao>()
        val employeeDao by inject<EmployeeDao>()
        val departmentsFacultiesDao by inject<DepartmentsFacultiesDao>()

        get {
            val principal = call.userPrincipal()
            val parameters = call.request.queryParameters

            val workIds = parameters.getIntList(
                key = "workIds",
                defaultValue = emptyList(),
                maxSize = MAX_ITEMS_SIZE
            )

            val offset = parameters.getInt("offset")
            val limit = parameters.getInt(key = "limit", range = LimitRange)
            val extended = parameters.getBoolean("extended", false)

            val departmentIds = if (principal.type == Employee.TYPE_ADMIN) {
                val facultyId = principal.facultyId ?: throw UnknownTokenException
                departmentsFacultiesDao.getDepartmentIdsByFacultyId(facultyId)
            } else principal.departmentIds

            val works = if (workIds.isEmpty()) {
                workDao.allWorks(departmentIds, offset, limit ?: MAX_ITEMS_SIZE)
            } else {
                workDao.allWorksByIds(workIds)
            }

            if (!extended) {
                respondSuccess { WorksResponse(works = works) }
            } else {
                val disciplineIds = works.map(Work::disciplineId)
                val disciplines = disciplineDao.allDisciplinesByIds(disciplineIds)

                val studentIds = works.map(Work::studentId)
                val students = studentDao.allStudentsByIds(studentIds)

                val workTypeIds = works.map(Work::workTypeId)
                val workTypes = workTypeDao.allWorkTypesByIds(workTypeIds)

                val departments = departmentDao.allDepartmentsByIds(departmentIds)

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
        val workDao by inject<WorkDao>()
        val disciplineDao by inject<DisciplineDao>()
        val studentDao by inject<StudentDao>()
        val workTypeDao by inject<WorkTypeDao>()
        val departmentDao by inject<DepartmentDao>()
        val employeeDao by inject<EmployeeDao>()

        get("{id}") {
            val principal = call.userPrincipal()
            val workId = call.parameters.getIntOrThrow("id")

            // TODO: 21/06/2024, Danil Nikolaev: check access ^

            val extended = call.request.queryParameters.getBoolean("extended", false)

            val work = workDao.singleWork(workId) ?: throw ContentNotFoundException

            if (!extended) {
                respondSuccess { WorkResponse(work = work) }
            } else {
                val discipline = disciplineDao.singleDiscipline(work.disciplineId) ?: throw ContentNotFoundException
                val student = studentDao.singleStudent(work.studentId)
                val workType = workTypeDao.singleWorkType(work.workTypeId)
                val department = departmentDao.singleDepartment(discipline.departmentId)
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

    // TODO: 12/06/2024, Danil Nikolaev: validate every field
    private fun Route.addWork() {
        val workDao by inject<WorkDao>()

        post {
            val principal = call.userPrincipal()
            val parameters = call.receiveParameters()

            val disciplineId = parameters.getIntOrThrow("disciplineId")
            val studentId = parameters.getIntOrThrow("studentId")
            val title = parameters.getString("title")
            val workTypeId = parameters.getIntOrThrow("workTypeId")
            val employeeId = parameters.getIntOrThrow("employeeId")

            // TODO: 20/06/2024, Danil Nikolaev: get student and validate that student is learning
            val created = workDao.addNewWork(
                disciplineId = disciplineId,
                studentId = studentId,
                registrationDate = System.currentTimeMillis(),
                title = title,
                workTypeId = workTypeId,
                employeeId = employeeId,
            )

            if (created != null) {
                respondSuccess { created }
            } else {
                throw UnknownException
            }
        }
    }

    // TODO: 12/06/2024, Danil Nikolaev: validate every field
    private fun Route.editWork() {
        val workDao by inject<WorkDao>()

        patch("{id}") {
            val principal = call.userPrincipal()
            val workId = call.parameters.getIntOrThrow("id")
            val currentWork = workDao.singleWork(workId) ?: throw ContentNotFoundException

            val parameters = call.receiveParameters()

            val disciplineId = parameters.getInt("disciplineId")
            val studentId = parameters.getInt("studentId")
            val registrationDate = parameters.getLong("registrationDate")
            val title = parameters.getString("title")
            val workTypeId = parameters.getInt("workTypeId")
            val employeeId = parameters.getInt("employeeId")

            workDao.updateWork(
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

    // TODO: 12/06/2024, Danil Nikolaev: validate every field
    private fun Route.deleteWorkById() {
        val workDao by inject<WorkDao>()

        delete("{id}") {
            val principal = call.userPrincipal()
            val workId = call.parameters.getIntOrThrow("id")
            workDao.singleWork(workId) ?: throw ContentNotFoundException

            if (workDao.deleteWork(workId)) {
                respondSuccess { 1 }
            } else {
                throw UnknownException
            }
        }
    }

    // TODO: 12/06/2024, Danil Nikolaev: validate every field
    private fun Route.deleteWorksByIds() {
        val workDao by inject<WorkDao>()

        delete {
            val principal = call.userPrincipal()
            val workIds = call.request.queryParameters.getIntListOrThrow(
                key = "workIds",
                requiredNotEmpty = true
            )

            val currentWorks = workDao.allWorksByIds(workIds)
            if (currentWorks.isEmpty()) {
                throw ContentNotFoundException
            }

            if (workDao.deleteWorks(workIds)) {
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
        val workTypeDao by inject<WorkTypeDao>()

        get("/worktypes") {
            val workTypesFilters = workTypeDao.allWorkTypesAsFilters()
            respondSuccess { workTypesFilters }
        }
    }

    private fun Route.getDisciplinesFilters() {
        val disciplineDao by inject<DisciplineDao>()
        val departmentsFacultiesDao by inject<DepartmentsFacultiesDao>()

        get("/disciplines") {
            val principal = call.userPrincipal()

            val departmentIds = if (principal.type == Employee.TYPE_ADMIN) {
                val facultyId = principal.facultyId ?: throw UnknownTokenException
                departmentsFacultiesDao.getDepartmentIdsByFacultyId(facultyId)
            } else principal.departmentIds

            val disciplinesFilters = disciplineDao.allDisciplinesAsFilters(departmentIds)

            respondSuccess { disciplinesFilters }
        }
    }

    private fun Route.getEmployeesFilters() {
        val employeeDao by inject<EmployeeDao>()
        val departmentsFacultiesDao by inject<DepartmentsFacultiesDao>()

        get("/employees") {
            val principal = call.userPrincipal()

            val departmentIds = if (principal.type == Employee.TYPE_ADMIN) {
                val facultyId = principal.facultyId ?: throw UnknownTokenException
                departmentsFacultiesDao.getDepartmentIdsByFacultyId(facultyId)
            } else principal.departmentIds

            val employeeFilters = employeeDao.allTeachers(null, null, departmentIds).map { employee ->
                EntryFilter(
                    id = employee.id,
                    title = employee.fullName
                )
            }

            respondSuccess { employeeFilters }
        }
    }

    private fun Route.getGroupsFilters() {
        val groupDao by inject<GroupDao>()

        get("/groups") {
            val principal = call.userPrincipal()
            val groupsFilters = groupDao.allGroupsAsFilters(principal.facultyId)

            respondSuccess { groupsFilters }
        }
    }

    private fun Route.getDepartmentsFilters() {
        val departmentDao by inject<DepartmentDao>()
        val departmentsFacultiesDao by inject<DepartmentsFacultiesDao>()

        get("/departments") {
            val principal = call.userPrincipal()

            val departmentIds = if (principal.type == Employee.TYPE_ADMIN) {
                val facultyId = principal.facultyId ?: throw UnknownTokenException
                departmentsFacultiesDao.getDepartmentIdsByFacultyId(facultyId)
            } else principal.departmentIds

            val departmentsFilters = departmentDao.allDepartmentsAsFilters(departmentIds)

            respondSuccess { departmentsFilters }
        }
    }

    private fun Route.getLatestWorks() {
        val workDao by inject<WorkDao>()
        val departmentsFacultiesDao by inject<DepartmentsFacultiesDao>()

        get {
            val principal = call.userPrincipal()
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

            val departmentIds = if (principal.type == Employee.TYPE_ADMIN) {
                val facultyId = principal.facultyId ?: throw UnknownTokenException
                departmentsFacultiesDao.getDepartmentIdsByFacultyId(facultyId)
            } else principal.departmentIds

            val entries = workDao.allWorksBySearch(
                departmentIds = departmentIds,
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

    companion object {
        context(Route)
        fun routes() {
            val controller by inject<WorkController>()
            controller.routes()
        }
    }
}

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
    val status: Int
)

data class EntryWork(
    val id: Int,
    val type: WorkType,
    val registrationDate: Long,
    val title: String?,
    val employeeId: Int
)

fun Student.mapToEntryStudent(): EntryStudent =
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
