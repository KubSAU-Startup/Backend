package com.meloda.kubsau.route.works

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.common.getInt
import com.meloda.kubsau.common.getIntOrThrow
import com.meloda.kubsau.common.getOrThrow
import com.meloda.kubsau.common.getString
import com.meloda.kubsau.database.departments.DepartmentsDao
import com.meloda.kubsau.database.disciplines.DisciplinesDao
import com.meloda.kubsau.database.employees.EmployeesDao
import com.meloda.kubsau.database.groups.GroupsDao
import com.meloda.kubsau.database.works.WorksDao
import com.meloda.kubsau.database.worktypes.WorkTypesDao
import com.meloda.kubsau.errors.ContentNotFoundException
import com.meloda.kubsau.errors.UnknownException
import com.meloda.kubsau.errors.ValidationException
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

private fun Route.getWorks() {
    val worksDao by inject<WorksDao>()

    get {
        val parameters = call.request.queryParameters

        val workIds = parameters["workIds"]
            ?.split(",")
            ?.map(String::trim)
            ?.mapNotNull(String::toIntOrNull)
            ?: emptyList()

        val works = if (workIds.isEmpty()) {
            worksDao.allWorks()
        } else {
            worksDao.allWorksByIds(workIds)
        }

        respondSuccess { works }
    }
}

private fun Route.getWorkById() {
    val worksDao by inject<WorksDao>()

    get("{id}") {
        val workId = call.parameters.getIntOrThrow("id")
        val work = worksDao.singleWork(workId) ?: throw ContentNotFoundException

        respondSuccess { work }
    }
}

private fun Route.addWork() {
    val worksDao by inject<WorksDao>()

    post {
        val parameters = call.receiveParameters()

        val disciplineId = parameters.getIntOrThrow("disciplineId")
        val studentId = parameters.getIntOrThrow("studentId")
        val registrationDate = parameters.getIntOrThrow("registrationDate")
        val title = parameters.getString("title")
        val workTypeId = parameters.getIntOrThrow("workTypeId")
        val employeeId = parameters.getIntOrThrow("employeeId")

        val created = worksDao.addNewWork(
            disciplineId = disciplineId,
            studentId = studentId,
            registrationDate = registrationDate * 1000L,
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
        val registrationDate = parameters.getInt("registrationDate")
        val title = parameters.getString("title")
        val workTypeId = parameters.getInt("workTypeId")
        val employeeId = parameters.getInt("employeeId")

        worksDao.updateWork(
            workId = workId,
            disciplineId = disciplineId ?: currentWork.disciplineId,
            studentId = studentId ?: currentWork.studentId,
            registrationDate = registrationDate?.let { it * 1000L } ?: currentWork.registrationDate,
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
        val workIds = call.request.queryParameters.getOrThrow("workIds")
            .split(",")
            .map(String::trim)
            .mapNotNull(String::toIntOrNull)

        if (workIds.isEmpty()) {
            throw ValidationException("workIds is invalid")
        }

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
    val employeesDao by inject<EmployeesDao>()

    get("/employees") {
        val employeeFilters = employeesDao.allEmployeesAsFilters()

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

        val workTypeId = parameters.getInt("workTypeId")
        val disciplineId = parameters.getInt("disciplineId")
        val employeeId = parameters.getInt("employeeId")
        val departmentId = parameters.getInt("departmentId")
        val groupId = parameters.getInt("groupId")
        val studentId = parameters.getInt("studentId")

        val journal = worksDao.allWorksByFilters(
            disciplineId = disciplineId,
            studentId = studentId,
            groupId = groupId,
            employeeId = employeeId,
            departmentId = departmentId,
            workTypeId = workTypeId
        )

        respondSuccess {
            GetJournalResponse(
                count = journal.size,
                offset = 0,
                journal = journal
            )
        }
    }
}

data class GetJournalResponse(
    val count: Int,
    val offset: Int,
    val journal: List<JournalItem>
)

fun Student.mapToJournalStudent(): JournalStudent =
    JournalStudent(
        id = id,
        fullName = fullName
    )

fun Work.mapToJournalWork(workType: WorkType): JournalWork =
    JournalWork(
        id = id,
        type = workType,
        registrationDate = registrationDate,
        title = title,
        employeeId = employeeId
    )

data class JournalFilter(
    val id: Int,
    val title: String
)

data class JournalItem(
    val student: JournalStudent,
    val group: Group,
    val discipline: Discipline,
    val employee: Employee,
    val work: JournalWork,
    val department: Department
)

data class JournalStudent(
    val id: Int,
    val fullName: String
)

data class JournalWork(
    val id: Int,
    val type: WorkType,
    val registrationDate: Long,
    val title: String?,
    val employeeId: Int
)
