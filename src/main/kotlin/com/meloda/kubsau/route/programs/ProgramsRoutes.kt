package com.meloda.kubsau.route.programs

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.common.*
import com.meloda.kubsau.database.departments.DepartmentsDao
import com.meloda.kubsau.database.programs.ProgramsDao
import com.meloda.kubsau.database.programsdisciplines.ProgramsDisciplinesDao
import com.meloda.kubsau.errors.ContentNotFoundException
import com.meloda.kubsau.errors.UnknownException
import com.meloda.kubsau.errors.ValidationException
import com.meloda.kubsau.model.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.programsRoutes() {
    authenticate {
        route("/programs") {
            getPrograms()
            getProgramById()
            getDisciplines()
            searchPrograms()
            addProgram()
            addDisciplinesToProgram()
            editProgram()
            editProgramDisciplines()
            deleteProgramById()
            deleteProgramsByIds()
        }
    }
}

private data class ProgramWithDisciplineIds(
    val program: Program,
    val disciplineIds: List<Int>
)

private fun Route.getPrograms() {
    val programsDao by inject<ProgramsDao>()
    val programsDisciplinesDao by inject<ProgramsDisciplinesDao>()

    get {
        val parameters = call.request.queryParameters

        val programIds = parameters.getIntList(
            key = "programIds",
            maxSize = MAX_PROGRAMS
        )

        val offset = parameters.getInt("offset")
        val limit = parameters.getInt(key = "limit", range = ProgramRange)

        val entries = programsDao.allProgramsBySearch(
            programIds = programIds,
            offset = offset,
            limit = limit ?: MAX_PROGRAMS,
            semester = null,
            directivityId = null,
            query = null
        )

        val disciplines = programsDisciplinesDao.allSearchDisciplinesByProgramIds(entries.map { it.program.id })

        respondSuccess {
            SearchResponse(
                count = entries.size,
                offset = offset ?: 0,
                entries = entries.map { entry ->
                    entry.copy(
                        disciplines = disciplines.filter { it.programId == entry.program.id }.map { it.discipline }
                    )
                }
            )
        }
    }
}

private fun Route.getProgramById() {
    val programsDao by inject<ProgramsDao>()
    val programsDisciplinesDao by inject<ProgramsDisciplinesDao>()

    get("{id}") {
        val programId = call.parameters.getIntOrThrow("id")
        val program = programsDao.singleProgram(programId) ?: throw ContentNotFoundException

        val disciplines = programsDisciplinesDao.allDisciplinesByProgramId(programId)
            .map(Discipline::id)

        respondSuccess {
            ProgramWithDisciplineIds(
                program = program,
                disciplineIds = disciplines
            )
        }
    }
}

private data class DisciplinesResponse(
    val disciplines: List<Discipline>
)

private data class FullDisciplinesResponse(
    val disciplines: List<Discipline>,
    val departments: List<Department>
)

private fun Route.getDisciplines() {
    val programsDisciplinesDao by inject<ProgramsDisciplinesDao>()
    val departmentsDao by inject<DepartmentsDao>()

    get("{programId}/disciplines") {
        val programId = call.parameters.getIntOrThrow("programId")
        val extended = call.request.queryParameters.getBoolean("extended", false)

        val disciplines = programsDisciplinesDao.allDisciplinesByProgramId(programId)

        if (!extended) {
            respondSuccess {
                DisciplinesResponse(disciplines = disciplines)
            }
        } else {
            val departmentIds = disciplines.map(Discipline::departmentId)
            val departments = departmentsDao.allDepartmentsByIds(departmentIds)

            respondSuccess {
                FullDisciplinesResponse(
                    disciplines = disciplines,
                    departments = departments
                )
            }
        }
    }

    get("/disciplines") {
        val parameters = call.request.queryParameters

        val programIds = parameters.getIntListOrThrow(
            key = "programIds",
            requiredNotEmpty = true
        )

        val extended = parameters.getBoolean("extended", false)

        val disciplines = programsDisciplinesDao.allDisciplinesByProgramIds(programIds)

        if (!extended) {
            respondSuccess { DisciplinesResponse(disciplines = disciplines) }
        } else {
            val departmentIds = disciplines.map(Discipline::departmentId)
            val departments = departmentsDao.allDepartmentsByIds(departmentIds)

            respondSuccess {
                FullDisciplinesResponse(
                    disciplines = disciplines,
                    departments = departments
                )
            }
        }
    }
}

private data class SearchResponse(
    val count: Int,
    val offset: Int,
    val entries: List<SearchEntry>
)

data class SearchEntry(
    val program: SearchProgram,
    val directivity: IdTitle,
    val grade: IdTitle,
    val disciplines: List<SearchDiscipline>
)

data class SearchProgram(
    val id: Int,
    val semester: Int
)

data class SearchDiscipline(
    val id: Int,
    val title: String,
    val workTypeId: Int,
    val departmentId: Int
)

data class SearchDisciplineWithProgramId(
    val programId: Int,
    val discipline: SearchDiscipline
)

private fun Route.searchPrograms() {
    val programsDao by inject<ProgramsDao>()
    val programsDisciplinesDao by inject<ProgramsDisciplinesDao>()

    get("/search") {
        val parameters = call.request.queryParameters

        val offset = parameters.getInt("offset")
        val limit = parameters.getInt(key = "limit", range = ProgramRange)
        val semester = parameters.getInt("semester")
        val directivityId = parameters.getInt("directivityId")
        val query = parameters.getString(key = "query", trim = true)?.lowercase()

        val entries = programsDao.allProgramsBySearch(
            programIds = null,
            offset = offset,
            limit = limit ?: MAX_PROGRAMS,
            semester = semester,
            directivityId = directivityId,
            query = query
        )

        val programIds = entries.map { it.program.id }
        val disciplines = programsDisciplinesDao.allSearchDisciplinesByProgramIds(programIds)

        respondSuccess {
            SearchResponse(
                count = entries.size,
                offset = offset ?: 0,
                entries = entries.map { entry ->
                    entry.copy(
                        disciplines = disciplines.filter { it.programId == entry.program.id }.map { it.discipline }
                    )
                }
            )
        }
    }
}

private fun Route.addProgram() {
    val programsDao by inject<ProgramsDao>()

    post {
        val parameters = call.receiveParameters()

        val semester = parameters.getIntOrThrow("semester")
        val directivityId = parameters.getIntOrThrow("directivityId")

        val created = programsDao.addNewProgram(
            semester = semester,
            directivityId = directivityId
        )

        if (created != null) {
            respondSuccess { created }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.addDisciplinesToProgram() {
    val programsDao by inject<ProgramsDao>()
    val programsDisciplinesDao by inject<ProgramsDisciplinesDao>()

    post("{id}/disciplines") {
        val programId = call.parameters.getIntOrThrow("id")
        programsDao.singleProgram(programId) ?: throw ContentNotFoundException

        val parameters = call.receiveParameters()

        val disciplineIds = parameters.getIntListOrThrow(
            key = "disciplineIds",
            requiredNotEmpty = true
        )

        val workTypeIds = parameters.getIntListOrThrow(
            key = "workTypeIds",
            requiredNotEmpty = true
        )

        if (disciplineIds.size != workTypeIds.size) {
            throw ValidationException.InvalidException(
                message = "different count of disciplines (${disciplineIds.size}) and work types (${workTypeIds.size}"
            )
        }

        disciplineIds.forEachIndexed { index, disciplineId ->
            if (!programsDisciplinesDao.addNewReference(
                    programId = programId,
                    disciplineId = disciplineId,
                    workTypeId = workTypeIds[index]
                )
            ) {
                throw UnknownException
            }
        }

        respondSuccess { 1 }
    }
}

private fun Route.editProgram() {
    val programsDao by inject<ProgramsDao>()

    patch("{id}") {
        val programId = call.parameters.getIntOrThrow("id")
        val currentProgram = programsDao.singleProgram(programId) ?: throw ContentNotFoundException

        val parameters = call.receiveParameters()

        val semester = parameters.getInt("semester")
        val directivityId = parameters.getInt("directivityId")

        programsDao.updateProgram(
            programId = programId,
            semester = semester ?: currentProgram.semester,
            directivityId = directivityId ?: currentProgram.directivityId
        ).let { success ->
            if (success) {
                respondSuccess { 1 }
            } else {
                throw UnknownException
            }
        }
    }
}

private fun Route.editProgramDisciplines() {
    val programsDao by inject<ProgramsDao>()
    val programsDisciplinesDao by inject<ProgramsDisciplinesDao>()

    patch("{id}/disciplines") {
        val programId = call.parameters.getIntOrThrow("id")
        val program = programsDao.singleProgram(programId) ?: throw ContentNotFoundException

        val parameters = call.receiveParameters()

        val disciplineIds = parameters.getIntListOrThrow(
            key = "disciplineIds",
            requiredNotEmpty = true
        )

        val workTypeIds = parameters.getIntListOrThrow(
            key = "workTypeIds",
            requiredNotEmpty = true
        )

        if (disciplineIds.size != workTypeIds.size) {
            throw ValidationException.InvalidException(
                message = "disciplines size (${disciplineIds.size}) is different from work types size (${workTypeIds.size})"
            )
        }

        programsDisciplinesDao.deleteReferencesByProgramId(program.id)

        disciplineIds.forEachIndexed { index, disciplineId ->
            programsDisciplinesDao.addNewReference(programId, disciplineId, workTypeIds[index])
        }

        respondSuccess { 1 }
    }
}

private fun Route.deleteProgramById() {
    val programsDao by inject<ProgramsDao>()

    delete("{id}") {
        val programId = call.parameters.getIntOrThrow("id")
        programsDao.singleProgram(programId) ?: throw ContentNotFoundException

        if (programsDao.deleteProgram(programId)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.deleteProgramsByIds() {
    val programsDao by inject<ProgramsDao>()

    delete {
        val programIds = call.request.queryParameters.getIntListOrThrow(
            key = "programIds",
            requiredNotEmpty = true
        )

        val currentPrograms = programsDao.allProgramsByIds(programIds)
        if (currentPrograms.isEmpty()) {
            throw ContentNotFoundException
        }

        if (programsDao.deletePrograms(programIds)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}
