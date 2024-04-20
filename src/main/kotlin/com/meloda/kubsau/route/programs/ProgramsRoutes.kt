package com.meloda.kubsau.route.programs

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.database.programs.ProgramsDao
import com.meloda.kubsau.database.programsdisciplines.ProgramsDisciplinesDao
import com.meloda.kubsau.errors.ContentNotFoundException
import com.meloda.kubsau.errors.UnknownException
import com.meloda.kubsau.errors.ValidationException
import com.meloda.kubsau.model.Discipline
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
            editProgram()
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
        }.map { program -> program.copy(disciplineIds = disciplineIds[program.id].orEmpty()) }

        respondSuccess { programs }
    }
}

private fun Route.getProgramById() {
    val programsDao by inject<ProgramsDao>()
    val programsDisciplinesDao by inject<ProgramsDisciplinesDao>()

    get("{id}") {
        val programId = call.parameters["id"]?.toIntOrNull() ?: throw ValidationException("id is empty")
        val program = programsDao.singleProgram(programId) ?: throw ContentNotFoundException

        val disciplines = programsDisciplinesDao.allDisciplinesByProgramId(programId)
            .map(Discipline::id)

        respondSuccess { program.copy(disciplineIds = disciplines) }
    }
}

private fun Route.addProgram() {
    val programsDao by inject<ProgramsDao>()
    val programsDisciplinesDao by inject<ProgramsDisciplinesDao>()

    post {
        val parameters = call.receiveParameters()

        val title = parameters["title"]?.trim() ?: throw ValidationException("title is empty")
        val semester = parameters["semester"]?.toIntOrNull() ?: throw ValidationException("semester is empty")
        val disciplineIds =
            parameters["disciplineIds"]
                ?.split(",")
                ?.mapNotNull(String::toIntOrNull)

        if (disciplineIds.isNullOrEmpty()) {
            throw ValidationException("disciplineIds is empty")
        }

        val created = programsDao.addNewProgram(
            title = title,
            semester = semester
        )

        if (created != null) {
            disciplineIds.forEach { disciplineId ->
                programsDisciplinesDao.addNewReference(
                    programId = created.id, disciplineId = disciplineId
                )
            }

            respondSuccess { created.copy(disciplineIds = disciplineIds) }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.editProgram() {
    val programsDao by inject<ProgramsDao>()
    val programsDisciplinesDao by inject<ProgramsDisciplinesDao>()

    patch("{id}") {
        val programId = call.parameters["id"]?.toIntOrNull() ?: throw ValidationException("id is empty")
        val currentProgram = programsDao.singleProgram(programId) ?: throw ContentNotFoundException

        val parameters = call.receiveParameters()

        val title = parameters["title"]?.trim()
        val semester = parameters["semester"]?.toIntOrNull()
        val disciplineIds =
            parameters["disciplineIds"]
                ?.split(",")
                ?.mapNotNull(String::toIntOrNull)

        disciplineIds
            ?.apply {
                programsDisciplinesDao.deleteReferencesByProgramId(programId)
            }?.forEach { disciplineId ->
                programsDisciplinesDao.addNewReference(programId, disciplineId)
            }

        programsDao.updateProgram(
            programId = programId,
            title = title ?: currentProgram.title,
            semester = semester ?: currentProgram.semester
        ).let { changedCount ->
            if (changedCount == 1) {
                respondSuccess { 1 }
            } else {
                throw UnknownException
            }
        }
    }
}

private fun Route.deleteProgramById() {
    val programsDao by inject<ProgramsDao>()

    delete("{id}") {
        val programId = call.parameters["id"]?.toIntOrNull() ?: throw ValidationException("id is empty")
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
        val programIds = call.request.queryParameters["programIds"]
            ?.split(",")
            ?.map(String::trim)
            ?.mapNotNull(String::toIntOrNull)
            ?: throw ValidationException("programIds is empty")

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
