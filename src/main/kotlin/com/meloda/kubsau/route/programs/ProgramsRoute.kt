package com.meloda.kubsau.route.programs

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.database.programs.ProgramsDao
import com.meloda.kubsau.errors.ContentNotFoundException
import com.meloda.kubsau.errors.UnknownException
import com.meloda.kubsau.errors.ValidationException
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

    get {
        val programIds = call.request.queryParameters["programIds"]
            ?.split(",")
            ?.map(String::trim)
            ?.mapNotNull(String::toIntOrNull)
            ?: emptyList()

        val programs = if (programIds.isEmpty()) {
            programsDao.allPrograms()
        } else {
            programsDao.allProgramsByIds(programIds)
        }

        respondSuccess { programs }
    }
}

private fun Route.getProgramById() {
    val programsDao by inject<ProgramsDao>()

    get("{id}") {
        val programId = call.parameters["id"]?.toIntOrNull() ?: throw ValidationException("id is empty")
        val program = programsDao.singleProgram(programId) ?: throw ContentNotFoundException

        respondSuccess { program }
    }
}

private fun Route.addProgram() {
    val programsDao by inject<ProgramsDao>()

    post {
        val parameters = call.receiveParameters()

        val title = parameters["title"]?.trim() ?: throw ValidationException("title is empty")
        val semester = parameters["semester"]?.toIntOrNull() ?: throw ValidationException("semester is empty")

        val created = programsDao.addNewProgram(
            title = title,
            semester = semester
        )

        if (created != null) {
            respondSuccess { created }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.editProgram() {
    val programsDao by inject<ProgramsDao>()

    patch("{id}") {
        val programId = call.parameters["id"]?.toIntOrNull() ?: throw ValidationException("id is empty")
        val currentProgram = programsDao.singleProgram(programId) ?: throw ContentNotFoundException

        val parameters = call.receiveParameters()

        val title = parameters["title"]?.trim()
        val semester = parameters["semester"]?.toIntOrNull()

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
