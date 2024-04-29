package com.meloda.kubsau.route.journal

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.common.getInt
import com.meloda.kubsau.database.departments.DepartmentsDao
import com.meloda.kubsau.database.disciplines.DisciplinesDao
import com.meloda.kubsau.database.employees.EmployeesDao
import com.meloda.kubsau.database.groups.GroupsDao
import com.meloda.kubsau.database.works.WorksDao
import com.meloda.kubsau.database.worktypes.WorkTypesDao
import com.meloda.kubsau.model.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.journalsRoutes() {
    authenticate {
        route("/journals") {
            getFilters()
            getJournals()
        }
    }
}

private fun Route.getFilters() {
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

private fun Route.getJournals() {
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
//    val workTypesDao by inject<WorkTypesDao>()
//
//    get {
//        val workTypes = workTypesDao.allWorkTypes()
//
//        val parameters = call.request.queryParameters
//
//        val journalIds = parameters["journalIds"]
//            ?.split(",")
//            ?.map(String::trim)
//            ?.mapNotNull(String::toIntOrNull)
//            ?: emptyList()
//
//        val workTypeId = parameters["workTypeId"]?.toIntOrNull()
//        val disciplineId = parameters["disciplineId"]?.toIntOrNull()
//        val teacherId = parameters["teacherId"]?.toIntOrNull()
//        val departmentId = parameters["departmentId"]?.toIntOrNull()
//        val groupId = parameters["groupId"]?.toIntOrNull()
//        val studentId = parameters["studentId"]?.toIntOrNull()
//
//        val journals =
//            if (journalIds.isEmpty()) {
//                journalsDao.allJournalsByFilters(
//                    studentId = studentId,
//                    groupId = groupId,
//                    disciplineId = disciplineId,
//                    teacherId = teacherId,
//                    workId = null,
//                    departmentId = departmentId,
//                    workTypeId = workTypeId
//                )
//            } else {
//                journalsDao.allJournalsByIdsWithFilters(
//                    journalIds = journalIds,
//                    studentId = studentId,
//                    groupId = groupId,
//                    disciplineId = disciplineId,
//                    teacherId = teacherId,
//                    workId = null,
//                    departmentId = departmentId,
//                    workTypeId = workTypeId
//                )
//            }
//
//        respondSuccess {
//            GetJournalResponse(
//                count = journals.size,
//                offset = 0,
//                journal = journals.mapNotNull { journal ->
//                    journal.mapToItem(workType = workTypes.find { it.id == journal.discipline.workTypeId })
//                }
//            )
//        }
//    }
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
        title = title
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
    val title: String?
)
