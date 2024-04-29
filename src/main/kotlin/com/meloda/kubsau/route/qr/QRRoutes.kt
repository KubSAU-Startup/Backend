package com.meloda.kubsau.route.qr

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.common.getIntOrThrow
import com.meloda.kubsau.common.getOrThrow
import com.meloda.kubsau.database.groups.GroupsDao
import com.meloda.kubsau.database.programs.ProgramsDao
import com.meloda.kubsau.database.programsdisciplines.ProgramsDisciplinesDao
import com.meloda.kubsau.database.students.StudentsDao
import com.meloda.kubsau.database.worktypes.WorkTypesDao
import com.meloda.kubsau.errors.ValidationException
import com.meloda.kubsau.model.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.qrRoutes() {
    authenticate {
        route("/qr") {
            groups()
            students()
            programs()
            disciplines()
        }
    }
}

private fun Route.groups() {
    val groupsDao by inject<GroupsDao>()

    route("/groups") {
        get {
            val groups = groupsDao.allGroups().map(Group::mapToShrankItem)
            respondSuccess { groups }
        }
    }
}

private fun Route.students() {
    val studentsDao by inject<StudentsDao>()

    get("/groups/{groupId}/students") {
        val groupId = call.parameters.getIntOrThrow("groupId")
        val students = studentsDao.allStudentsByGroupId(groupId)
        respondSuccess { students }
    }

    get("/groups/students") {
        val params = call.request.queryParameters
        val groupIds = params.getOrThrow("groupIds")
            .split(",")
            .map(String::trim)
            .mapNotNull(String::toIntOrNull)

        if (groupIds.isEmpty()) {
            throw ValidationException("groupId is invalid")
        }

        val fullStudents = studentsDao.allStudentsByGroupIds(groupIds)

        val listOfStudents = mutableListOf<GroupIdWithStudents>()

        groupIds.forEach { id ->
            fullStudents
                .filter { student -> student.groupId == id }
                .map(Student::mapToShrankItem)
                .let { shrankStudents -> GroupIdWithStudents(id, shrankStudents) }
                .let(listOfStudents::add)
        }

        respondSuccess { listOfStudents }
    }

    route("/students") {
        get {
            val students = studentsDao.allStudents().map(Student::mapToShrankItem)
            respondSuccess { students }
        }
    }
}

private fun Route.programs() {
    val programsDao by inject<ProgramsDao>()

    route("/programs") {
        get {
            val programs = programsDao.allPrograms().map(Program::mapToShrankItem)
            respondSuccess { programs }
        }
    }
}

private fun Route.disciplines() {
    val workTypesDao by inject<WorkTypesDao>()
    val programsDisciplinesDao by inject<ProgramsDisciplinesDao>()

    get("/programs/{programId}/disciplines") {
        val programId = call.parameters.getIntOrThrow("programId")
        val disciplines = programsDisciplinesDao.allDisciplinesByProgramId(programId)

        respondSuccess { disciplines }
    }

//    get("/programs/disciplines") {
//        val params = call.request.queryParameters
//
//        // TODO: 17/04/2024, Danil Nikolaev: shitty solution
//        val programIds = params["programIds"]
//            ?.split(",")
//            ?.map(String::trim)
//            ?.mapNotNull(String::toIntOrNull)
//            ?: programsDisciplinesDao.allReferences().map { (program, _) -> program.id }
//
//        val disciplines = if (programIds.isEmpty()) {
//            programsDisciplinesDao.allReferences().map { (_, discipline) -> discipline }
//        } else {
//            programsDisciplinesDao.allDisciplinesByProgramIds(programIds)
//        }

//        val response = if (extended) {
//            val workTypes = disciplines.map(Discipline::workTypeId).distinct().let { ids ->
//                workTypesDao.allWorkTypesByIds(ids)
//            }
//
//            val resultDisciplines = List(programIds.size) { index ->
//                DisciplinesWithWorkTypesWithProgramId(
//                    programId = programIds[index],
//                    disciplines = emptyList()
//                )
//            }.toMutableList()
//
//            disciplines.forEach { discipline ->
//                val programId = programsDisciplinesDao.programByDisciplineId(discipline.id)?.id ?: -1
//                val disciplinesList = resultDisciplines
//                    .firstOrNull { it.programId == programId }?.disciplines
//                    .orEmpty()
//                    .toMutableList()
//
//                disciplinesList += DisciplineWithWorkType(
//                    discipline = discipline,
//                    workType = workTypes.first { it.id == discipline.workTypeId }
//                )
//
//                resultDisciplines
//                    .indexOfFirst { it.programId == programId }
//                    .let { index ->
//                        if (index != -1) {
//                            resultDisciplines[index] = resultDisciplines[index].copy(disciplines = disciplinesList)
//                        }
//                    }
//            }
//
//            resultDisciplines
//        } else {
//
//        }
//
//        val resultDisciplines = List(programIds.size) { index ->
//            DisciplinesWithProgramId(
//                programId = programIds[index],
//                disciplines = emptyList()
//            )
//        }.toMutableList()
//
//        disciplines.forEach { discipline ->
//            // TODO: 17/04/2024, Danil Nikolaev: shitty solution, rewrite db
//            val programId = programsDisciplinesDao.programByDisciplineId(discipline.id)?.id ?: -1
//            val disciplinesList = resultDisciplines
//                .firstOrNull { it.programId == programId }?.disciplines
//                .orEmpty()
//                .toMutableList()
//
//            disciplinesList += discipline
//
//            resultDisciplines
//                .indexOfFirst { it.programId == programId }
//                .let { index ->
//                    if (index != -1) {
//                        resultDisciplines[index] = resultDisciplines[index].copy(disciplines = disciplinesList)
//                    }
//                }
//        }
//
//        respondSuccess { response }
//    }
}

private data class DisciplinesWithProgramId(
    val programId: Int,
    val disciplines: List<Discipline>
)

private data class DisciplinesWithWorkTypesWithProgramId(
    val programId: Int,
    val disciplines: List<DisciplineWithWorkType>
)

private data class DisciplineWithWorkType(
    val discipline: Discipline,
    val workType: WorkType
)

private fun Program.mapWithDisciplines(disciplines: List<Discipline>): ProgramWithDisciplines =
    ProgramWithDisciplines(
        id = id,
        title = title,
        semester = semester,
        disciplines = disciplines
    )


private fun Group.mapWithStudents(students: List<Student>): GroupWithStudents =
    GroupWithStudents(
        group = this,
        students = students
    )

private data class GroupWithStudents(
    val group: Group,
    val students: List<Student>
)

private data class ProgramWithDisciplines(
    val id: Int,
    val title: String,
    val semester: Int,
    val disciplines: List<Discipline>
)

private data class GetDataResponse(
    val programs: List<ProgramWithDisciplines>,
    val groups: List<GroupWithStudents>
)

private data class GroupIdWithStudents(
    val groupId: Int,
    val students: List<ShrankItem>
)

private data class ShrankItem(
    val id: Int,
    val title: String
)

private fun Group.mapToShrankItem(): ShrankItem =
    ShrankItem(id = id, title = title)

private fun Student.mapToShrankItem(): ShrankItem =
    ShrankItem(id = id, title = fullName)

private fun Program.mapToShrankItem(): ShrankItem =
    ShrankItem(id = id, title = title)
