package com.meloda.kubsau.route.programs

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.common.*
import com.meloda.kubsau.database.departments.DepartmentsDao
import com.meloda.kubsau.database.directivities.DirectivitiesDao
import com.meloda.kubsau.database.disciplines.DisciplinesDao
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
import kotlin.collections.set

fun Route.programsRoutes() {
    authenticate {
        route("/programs") {
            getPrograms()
            getProgramById()
            getDisciplines()
            getFiltered()
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

private data class ProgramWithDirectivityAndDisciplines(
    val program: Program,
    val directivity: Directivity,
    val disciplines: List<Discipline>
)

private data class ProgramsWithDisciplines(
    val count: Int,
    val offset: Int,
    val programs: List<ProgramWithDisciplineIds>,
    val disciplines: List<DisciplineWithWorkType>?
)

private data class Programs(
    val count: Int,
    val offset: Int,
    val programs: List<ProgramWithDisciplineIds>
)

private data class DisciplineWithWorkType(
    val discipline: Discipline,
    val workType: WorkType
)

private fun Route.getPrograms() {
    val programsDao by inject<ProgramsDao>()
    val programsDisciplinesDao by inject<ProgramsDisciplinesDao>()
    val disciplinesDao by inject<DisciplinesDao>()

    get {
        val parameters = call.request.queryParameters

        val programIds = parameters.getString("programIds")
            ?.split(",")
            ?.mapNotNull(String::toIntOrNull)
            ?: emptyList()

        val offset = parameters.getInt("offset")
        val limit = parameters.getInt("limit")
        val extended = parameters.getBoolean("extended", false)

        val disciplineIds = hashMapOf<Int, List<Int>>()

        programIds
            .ifEmpty {
                programsDao.allPrograms(offset, limit).map(Program::id)
            }.forEach { programId ->
                disciplineIds[programId] = programsDisciplinesDao.allDisciplinesByProgramId(programId)
                    .map(Discipline::id)
            }

        val programs = if (programIds.isEmpty()) {
            programsDao.allPrograms(offset, limit)
        } else {
            programsDao.allProgramsByIds(programIds)
        }.map { program ->
            ProgramWithDisciplineIds(
                program = program,
                disciplineIds = disciplineIds[program.id].orEmpty()
            )
        }

        val disciplines = mutableListOf<DisciplineWithWorkType>()

        if (extended) {
            programs.forEach { program ->
                program.disciplineIds.forEach { disciplineId ->
                    val workType = programsDisciplinesDao.workType(program.program.id, disciplineId)
                    val discipline = disciplinesDao.singleDiscipline(disciplineId)

                    if (workType != null && discipline != null) {
                        disciplines += DisciplineWithWorkType(
                            discipline = discipline,
                            workType = workType
                        )
                    }
                }
            }
        }

        respondSuccess {
            if (extended) {
                ProgramsWithDisciplines(
                    count = programs.size,
                    offset = offset ?: 0,
                    programs = programs,
                    disciplines = disciplines
                )
            } else {
                Programs(
                    count = programs.size,
                    offset = offset ?: 0,
                    programs = programs
                )
            }
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

        val programIds = parameters.getOrThrow("programIds")
            .split(",")
            .mapNotNull(String::toIntOrNull)

        if (programIds.isEmpty()) {
            throw ValidationException("programIds is invalid")
        }

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

private fun Route.getFiltered() {
    val programsDao by inject<ProgramsDao>()
    val programsDisciplinesDao by inject<ProgramsDisciplinesDao>()
    val disciplinesDao by inject<DisciplinesDao>()

    get("/filtered") {
        val parameters = call.request.queryParameters

        val offset = parameters.getInt("offset")
        val limit = parameters.getInt("limit")
        val extended = parameters.getBoolean("extended", false)
        val semester = parameters.getInt("semester")
        val directivityId = parameters.getInt("directivityId")

        val filteredPrograms = programsDao.allProgramsByFilters(
            offset = offset,
            limit = limit,
            semester = semester,
            directivityId = directivityId
        )

        val programIds = filteredPrograms.map(Program::id)

        val disciplineIds = hashMapOf<Int, List<Int>>()

        programIds
            .ifEmpty {
                programsDao.allPrograms(offset, limit).map(Program::id)
            }.forEach { programId ->
                disciplineIds[programId] = programsDisciplinesDao.allDisciplinesByProgramId(programId)
                    .map(Discipline::id)
            }

        val programs = filteredPrograms.map { program ->
            ProgramWithDisciplineIds(
                program = program,
                disciplineIds = disciplineIds[program.id].orEmpty()
            )
        }

        val disciplines = mutableListOf<DisciplineWithWorkType>()

        if (extended) {
            programs.forEach { program ->
                program.disciplineIds.forEach { disciplineId ->
                    val workType = programsDisciplinesDao.workType(program.program.id, disciplineId)
                    val discipline = disciplinesDao.singleDiscipline(disciplineId)

                    if (workType != null && discipline != null) {
                        disciplines += DisciplineWithWorkType(
                            discipline = discipline,
                            workType = workType
                        )
                    }
                }
            }
        }

        respondSuccess {
            if (extended) {
                ProgramsWithDisciplines(
                    count = programs.size,
                    offset = offset ?: 0,
                    programs = programs,
                    disciplines = disciplines
                )
            } else {
                Programs(
                    count = programs.size,
                    offset = offset ?: 0,
                    programs = programs
                )
            }
        }
    }
}

private data class SearchResponse(
    val count: Int,
    val offset: Int,
    val entries: List<ProgramWithDirectivityAndDisciplines>
)

private fun Route.searchPrograms() {
    val programsDao by inject<ProgramsDao>()
    val programsDisciplinesDao by inject<ProgramsDisciplinesDao>()
    val directivitiesDao by inject<DirectivitiesDao>()

    get("/search") {
        val parameters = call.request.queryParameters

        val offset = parameters.getInt("offset")
        val limit = parameters.getInt("limit")
        val query = parameters
            .getOrThrow("query")
            .lowercase()
            .trim()
            .ifEmpty { null }
            ?: throw ValidationException("query must not be empty or blank")

        val allPrograms = programsDao.allProgramsByQuery(
            offset = offset,
            limit = limit,
            query = query
        )

        val directivityIds = allPrograms.map(Program::directivityId)
        val directivities = directivitiesDao.allDirectivitiesByIds(directivityIds)

        val programIds = allPrograms.map(Program::id)
        val disciplines = hashMapOf<Int, List<Discipline>>()

        programIds.forEach { programId ->
            disciplines[programId] = programsDisciplinesDao.allDisciplinesByProgramId(programId)
        }

        val resultPrograms = allPrograms.map { program ->
            ProgramWithDirectivityAndDisciplines(
                program = program,
                directivity = directivities.first { directivity -> directivity.id == program.directivityId },
                disciplines = disciplines[program.id].orEmpty()
            )
        }

        respondSuccess {
            SearchResponse(
                count = resultPrograms.size,
                offset = offset ?: 0,
                entries = resultPrograms
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

        val disciplineIds =
            parameters.getOrThrow("disciplineIds")
                .split(",")
                .mapNotNull(String::toIntOrNull)

        if (disciplineIds.isEmpty()) {
            throw ValidationException("disciplineIds is invalid")
        }

        val workTypeIds =
            parameters.getOrThrow("workTypeIds")
                .split(",")
                .mapNotNull(String::toIntOrNull)

        if (workTypeIds.isEmpty()) {
            throw ValidationException("workTypeIds is invalid")
        }

        if (disciplineIds.size != workTypeIds.size) {
            throw ValidationException("different count of disciplines (${disciplineIds.size}) and work types (${workTypeIds.size}")
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
        val programId = call.parameters["id"]?.toIntOrNull() ?: throw ValidationException("id is empty")
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

        val disciplineIds =
            parameters.getOrThrow("disciplineIds")
                .split(",")
                .mapNotNull(String::toIntOrNull)

        if (disciplineIds.isEmpty()) {
            throw ValidationException("disciplineIds is invalid")
        }

        val workTypeIds =
            parameters.getOrThrow("workTypeIds")
                .split(",")
                .mapNotNull(String::toIntOrNull)

        if (workTypeIds.isEmpty()) {
            throw ValidationException("workTypeIds is invalid")
        }

        if (disciplineIds.size != workTypeIds.size) {
            throw ValidationException("disciplines size (${disciplineIds.size}) is different from work types size (${workTypeIds.size})")
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
        val programIds = call.request.queryParameters.getOrThrow("programIds")
            .split(",")
            .map(String::trim)
            .mapNotNull(String::toIntOrNull)

        if (programIds.isEmpty()) {
            throw ValidationException("programIds is invalid")
        }

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
