package com.meloda.kubsau.route.programs

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.common.getInt
import com.meloda.kubsau.common.getIntOrThrow
import com.meloda.kubsau.common.getOrThrow
import com.meloda.kubsau.common.getString
import com.meloda.kubsau.database.programs.ProgramsDao
import com.meloda.kubsau.database.programsdisciplines.ProgramsDisciplinesDao
import com.meloda.kubsau.errors.ContentNotFoundException
import com.meloda.kubsau.errors.UnknownException
import com.meloda.kubsau.errors.ValidationException
import com.meloda.kubsau.model.Discipline
import com.meloda.kubsau.model.Program
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
            addProgram()
            addDisciplinesToProgram()
            editProgram()
            editProgramDisciplines()
            deleteProgramById()
            deleteProgramsByIds()
        }
    }
}

private fun Route.getPrograms() {
    val programsDao by inject<ProgramsDao>()
    val programsDisciplinesDao by inject<ProgramsDisciplinesDao>()

    get {
        val programIds = call.request.queryParameters["programIds"]
            ?.split(",")
            ?.map(String::trim)
            ?.mapNotNull(String::toIntOrNull)
            ?: emptyList()

        val disciplineIds = hashMapOf<Int, List<Int>>()

        programIds.forEach { programId ->
            disciplineIds[programId] = programsDisciplinesDao.allDisciplinesByProgramId(programId)
                .map(Discipline::id)
        }

        val programs = if (programIds.isEmpty()) {
            programsDao.allPrograms()
        } else {
            programsDao.allProgramsByIds(programIds)
        }.map { program ->
            ProgramWithDisciplineIds(
                program = program,
                disciplineIds = disciplineIds[program.id].orEmpty()
            )
        }

        respondSuccess { programs }
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

private fun Route.addProgram() {
    val programsDao by inject<ProgramsDao>()

    post {
        val parameters = call.receiveParameters()

        val title = parameters.getOrThrow("title")
        val semester = parameters.getIntOrThrow("semester")
        val directivityId = parameters.getIntOrThrow("directivityId")

        val created = programsDao.addNewProgram(
            title = title,
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

        val title = parameters.getString("title")
        val semester = parameters.getInt("semester")
        val directivityId = parameters.getInt("directivityId")

        programsDao.updateProgram(
            programId = programId,
            title = title ?: currentProgram.title,
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

private data class ProgramWithDisciplineIds(
    val program: Program,
    val disciplineIds: List<Int>
)
