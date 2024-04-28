package com.meloda.kubsau.route.journal

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.database.departments.DepartmentsDao
import com.meloda.kubsau.database.disciplines.DisciplinesDao
import com.meloda.kubsau.database.groups.GroupsDao
import com.meloda.kubsau.database.journals.JournalsDao
import com.meloda.kubsau.database.teachers.TeachersDao
import com.meloda.kubsau.database.worktypes.WorkTypesDao
import com.meloda.kubsau.errors.ContentNotFoundException
import com.meloda.kubsau.errors.ValidationException
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
            getJournalById()
        }
    }
}

private fun Route.getFilters() {
    route("/filters") {
        getWorkTypesFilters()
        getDisciplinesFilters()
        getTeachersFilters()
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

private fun Route.getTeachersFilters() {
    val teachersDao by inject<TeachersDao>()

    get("/teachers") {
        val teachersFilters = teachersDao.allTeachersAsFilters()

        respondSuccess { teachersFilters }
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
    val journalsDao by inject<JournalsDao>()
    val workTypesDao by inject<WorkTypesDao>()

    get {
        val workTypes = workTypesDao.allWorkTypes()

        val parameters = call.request.queryParameters

        val journalIds = parameters["journalIds"]
            ?.split(",")
            ?.map(String::trim)
            ?.mapNotNull(String::toIntOrNull)
            ?: emptyList()

        val workTypeId = parameters["workTypeId"]?.toIntOrNull()
        val disciplineId = parameters["disciplineId"]?.toIntOrNull()
        val teacherId = parameters["teacherId"]?.toIntOrNull()
        val departmentId = parameters["departmentId"]?.toIntOrNull()
        val groupId = parameters["groupId"]?.toIntOrNull()
        val studentId = parameters["studentId"]?.toIntOrNull()

        val journals =
            if (journalIds.isEmpty()) {
                journalsDao.allJournalsByFilters(
                    studentId = studentId,
                    groupId = groupId,
                    disciplineId = disciplineId,
                    teacherId = teacherId,
                    workId = null,
                    departmentId = departmentId,
                    workTypeId = workTypeId
                )
            } else {
                journalsDao.allJournalsByIdsWithFilters(
                    journalIds = journalIds,
                    studentId = studentId,
                    groupId = groupId,
                    disciplineId = disciplineId,
                    teacherId = teacherId,
                    workId = null,
                    departmentId = departmentId,
                    workTypeId = workTypeId
                )
            }

        respondSuccess {
            GetJournalResponse(
                count = journals.size,
                offset = 0,
                journal = journals.mapNotNull { journal ->
                    journal.mapToItem(workType = workTypes.find { it.id == journal.discipline.workTypeId })
                }
            )
        }
    }
}

private fun Route.getJournalById() {
    val journalsDao by inject<JournalsDao>()
    val workTypesDao by inject<WorkTypesDao>()

    get("{id}") {
        val journalId = call.parameters["id"]?.toIntOrNull() ?: throw ValidationException("id is empty")
        val journal = journalsDao.singleById(journalId) ?: throw ContentNotFoundException
        val workType = workTypesDao.singleWorkType(journal.discipline.workTypeId)

        respondSuccess { journal.mapToItem(workType) }
    }
}

private data class GetJournalResponse(
    val count: Int,
    val offset: Int,
    val journal: List<JournalItem>
)

private fun Journal.mapToItem(workType: WorkType?): JournalItem? =
    if (workType == null) null
    else JournalItem(
        student = student.mapToJournalStudent(),
        group = group,
        discipline = discipline,
        teacher = teacher,
        work = work.mapToJournalWork(workType),
        department = department
    )

private fun Student.mapToJournalStudent(): JournalStudent =
    JournalStudent(
        id = id,
        fullName = fullName,
        status = status
    )

private fun Work.mapToJournalWork(workType: WorkType): JournalWork =
    JournalWork(
        id = id,
        type = workType,
        registrationDate = registrationDate,
        title = title
    )

private data class JournalFilters(
    val workTypes: List<JournalFilter>,
    val disciplines: List<JournalFilter>,
    val teachers: List<JournalFilter>,
    val groups: List<JournalFilter>,
    val departments: List<JournalFilter>
)

data class JournalFilter(
    val id: Int,
    val title: String
)

private data class JournalItem(
    val student: JournalStudent,
    val group: Group,
    val discipline: Discipline,
    val teacher: Teacher,
    val work: JournalWork,
    val department: Department?
)

private data class JournalStudent(
    val id: Int,
    val fullName: String,
    val status: Int
)

private data class JournalWork(
    val id: Int,
    val type: WorkType,
    val registrationDate: Long,
    val title: String?
)
