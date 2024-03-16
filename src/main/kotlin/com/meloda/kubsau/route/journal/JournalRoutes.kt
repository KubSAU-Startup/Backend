package com.meloda.kubsau.route.journal

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.database.departments.DepartmentsDao
import com.meloda.kubsau.database.worktypes.WorkTypesDao
import com.meloda.kubsau.model.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.journals() {
    val workTypesDao by inject<WorkTypesDao>()

    authenticate {
        route("/journals") {
            get("worktypes") {
                val workTypes = workTypesDao.allWorkTypes()
                respondSuccess { workTypes }
            }
            get {
                val params = call.request.queryParameters
                val workTypeId = params["workTypeId"]?.toInt()
                val disciplineId = params["disciplineId"]?.toInt()
                val teacherId = params["teacherId"]?.toInt()
                val departmentId = params["departmentId"]?.toInt()
                val groupId = params["groupId"]?.toInt()

                val filteredJournal = emptyList<JournalItem>()/*journal*/.filter { item ->
                    (item.work.type.id == workTypeId || workTypeId == null) &&
                            (item.discipline.id == disciplineId || disciplineId == null) &&
                            (item.teacher.id == teacherId || teacherId == null) &&
                            (item.group.id == groupId || groupId == null) &&
                            (item.teacher.departmentId == departmentId || departmentId == null)
                }

                respondSuccess {
                    GetJournalResponse(
                        count = filteredJournal.size,
                        offset = 0,
                        journal = filteredJournal
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
    val departmentsDao by inject<DepartmentsDao>()
    val workTypesDao by inject<WorkTypesDao>()

    get("/journals/filters") {
        val workTypes = workTypesDao.allWorkTypes()
        val departments = departmentsDao.allDepartments()

        respondSuccess {
            JournalFilters(
                workTypes = workTypes.map(WorkType::mapToFilter),
                disciplines = emptyList(),//disciplines.map(Discipline::mapToFilter),
                teachers = emptyList(),// teachers.map(Teacher::mapToFilter),
                groups = emptyList(),// groups.map(Group::mapToFilter),
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
    val work: JournalWork
)

private data class JournalStudent(
    val fullName: String,
    val status: Int
)

private data class JournalWork(
    val id: Int,
    val type: WorkType,
    val registrationDate: Long,
    val title: String?
)
