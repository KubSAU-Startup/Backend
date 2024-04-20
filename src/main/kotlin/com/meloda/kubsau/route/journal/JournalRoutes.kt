package com.meloda.kubsau.route.journal

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.database.departments.DepartmentsDao
import com.meloda.kubsau.database.disciplines.DisciplinesDao
import com.meloda.kubsau.database.groups.GroupsDao
import com.meloda.kubsau.database.journals.JournalsDao
import com.meloda.kubsau.database.teachers.TeachersDao
import com.meloda.kubsau.database.worktypes.WorkTypesDao
import com.meloda.kubsau.model.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.journalsRoutes() {
    val journalsDao by inject<JournalsDao>()
    val workTypesDao by inject<WorkTypesDao>()

    authenticate {
        route("/journals") {
            get {
                val workTypes = workTypesDao.allWorkTypes()

                val params = call.request.queryParameters
                val workTypeId = params["workTypeId"]?.toIntOrNull()
                val disciplineId = params["disciplineId"]?.toIntOrNull()
                val teacherId = params["teacherId"]?.toIntOrNull()
                val departmentId = params["departmentId"]?.toIntOrNull()
                val groupId = params["groupId"]?.toIntOrNull()
                val studentId = params["studentId"]?.toIntOrNull()

                val journals = journalsDao.allJournals(
                    journalId = null,
                    studentId = studentId,
                    groupId = groupId,
                    disciplineId = disciplineId,
                    teacherId = teacherId,
                    workId = null,
                    departmentId = departmentId,
                    workTypeId = workTypeId
                )

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

        filters()
    }
}

private data class GetJournalResponse(
    val count: Int,
    val offset: Int,
    val journal: List<JournalItem>
)

private fun Route.filters() {
    val workTypesDao by inject<WorkTypesDao>()
    val disciplinesDao by inject<DisciplinesDao>()
    val teachersDao by inject<TeachersDao>()
    val groupsDao by inject<GroupsDao>()
    val departmentsDao by inject<DepartmentsDao>()

    get("/journals/filters") {
        val workTypes = workTypesDao.allWorkTypes()
        val disciplines = disciplinesDao.allDisciplines()
        val teachers = teachersDao.allTeachers()
        val groups = groupsDao.allGroups()
        val departments = departmentsDao.allDepartments()

        respondSuccess {
            JournalFilters(
                workTypes = workTypes.map(WorkType::mapToFilter),
                disciplines = disciplines.map(Discipline::mapToFilter),
                teachers = teachers.map(Teacher::mapToFilter),
                groups = groups.map(Group::mapToFilter),
                departments = departments.map(Department::mapToFilter)
            )
        }
    }
}

private fun Filterable.mapToFilter(): JournalFilter =
    JournalFilter(
        id = this.id,
        title = this.title
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

private data class JournalFilter(
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
