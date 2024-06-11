package com.meloda.kubsau.route.heads

import com.meloda.kubsau.common.*
import com.meloda.kubsau.database.faculties.FacultiesDao
import com.meloda.kubsau.database.heads.HeadsDao
import com.meloda.kubsau.model.*
import com.meloda.kubsau.plugins.UserPrincipal
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.majorsRoutes() {
    authenticate {
        route("/heads") {
            getHeads()
            getHeadById()
            addHead()
            editHead()
            deleteHead()
            deleteHeads()
        }
    }
}

private data class HeadsResponse(val heads: List<Head>)

private data class FullHeadsResponse(
    val heads: List<Head>,
    val faculties: List<Faculty>
)

private fun Route.getHeads() {
    val headsDao by inject<HeadsDao>()
    val facultiesDao by inject<FacultiesDao>()

    get {
        val principal = call.principal<UserPrincipal>() ?: throw UnknownTokenException

        val parameters = call.request.queryParameters

        val headIds = parameters.getIntList(
            key = "headIds",
            defaultValue = emptyList(),
            maxSize = MAX_ITEMS_SIZE
        )

        val offset = parameters.getInt("offset")
        val limit = parameters.getInt(key = "limit", range = LimitRange)
        val extended = parameters.getBoolean("extended", false)

        val heads = if (headIds.isEmpty()) {
            headsDao.allHeads(
                facultyId = principal.facultyId,
                offset = offset,
                limit = limit ?: MAX_ITEMS_SIZE
            )
        } else {
            headsDao.allHeadsByIds(headIds)
        }

        if (principal.facultyId != null && headIds.isNotEmpty() &&
            heads.map(Head::facultyId).distinct().singleOrNull() != principal.facultyId
        ) {
            val unavailableHeadIds = heads.filter { head -> head.facultyId != principal.facultyId }.map(Head::id)
            throw AccessDeniedException("Unavailable headIds: ${unavailableHeadIds.joinToString()}")
        }

        if (!extended) {
            respondSuccess { HeadsResponse(heads = heads) }
        } else {
            val facultyIds = heads.map(Head::facultyId)
            val faculties = facultiesDao.allFacultiesByIds(facultyIds)

            respondSuccess {
                FullHeadsResponse(
                    heads = heads,
                    faculties = faculties
                )
            }
        }
    }
}

private data class HeadResponse(
    val head: Head
)

private data class FullHeadResponse(
    val head: Head,
    val faculty: Faculty
)

private fun Route.getHeadById() {
    val headsDao by inject<HeadsDao>()
    val facultiesDao by inject<FacultiesDao>()

    get("{id}") {
        val principal = call.principal<UserPrincipal>() ?: throw UnknownTokenException
        val headId = call.parameters.getIntOrThrow("id")
        val extended = call.request.queryParameters.getBoolean("extended", false)

        val head = headsDao.singleHead(headId) ?: throw ContentNotFoundException

        if (principal.facultyId != null && head.facultyId != principal.facultyId) {
            throw AccessDeniedException("Unavailable headId: $headId")
        }

        if (!extended) {
            respondSuccess { HeadResponse(head = head) }
        } else {
            val faculty = facultiesDao.singleFaculty(head.facultyId) ?: throw ContentNotFoundException

            respondSuccess {
                FullHeadResponse(
                    head = head,
                    faculty = faculty
                )
            }
        }
    }
}

private fun Route.addHead() {
    val headsDao by inject<HeadsDao>()

    post {
        val principal = call.principal<UserPrincipal>() ?: throw UnknownTokenException
        val parameters = call.receiveParameters()

        val code = parameters.getStringOrThrow("code")
        val abbreviation = parameters.getStringOrThrow("abbreviation")
        val title = parameters.getStringOrThrow("title")
        val facultyId = parameters.getIntOrThrow("facultyId")

        if (principal.facultyId != null && facultyId != principal.facultyId) {
            throw AccessDeniedException("Unavailable facultyId: $facultyId")
        }

        val created = headsDao.addNewHead(
            code = code,
            abbreviation = abbreviation,
            title = title,
            facultyId = facultyId
        )

        if (created != null) {
            respondSuccess { created }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.editHead() {
    val headsDao by inject<HeadsDao>()

    patch("{id}") {
        val principal = call.principal<UserPrincipal>() ?: throw UnknownTokenException
        val headId = call.parameters.getIntOrThrow("id")
        val currentHead = headsDao.singleHead(headId) ?: throw ContentNotFoundException

        val parameters = call.receiveParameters()

        val code = parameters.getString("code")
        val abbreviation = parameters.getString("abbreviation")
        val title = parameters.getString("title")
        val facultyId = parameters.getInt("facultyId")

        if (principal.facultyId != null && facultyId != null && facultyId != principal.facultyId) {
            throw AccessDeniedException("Unavailable facultyId: $facultyId")
        }

        headsDao.updateHead(
            headId = headId,
            code = code ?: currentHead.code,
            abbreviation = abbreviation ?: currentHead.abbreviation,
            title = title ?: currentHead.title,
            facultyId = facultyId ?: currentHead.facultyId
        ).let { success ->
            if (success) {
                respondSuccess { 1 }
            } else {
                throw UnknownException
            }
        }
    }
}

private fun Route.deleteHead() {
    val headsDao by inject<HeadsDao>()

    delete("{id}") {
        val principal = call.principal<UserPrincipal>() ?: throw UnknownTokenException
        val headId = call.parameters.getIntOrThrow("id")
        val currentHead = headsDao.singleHead(headId) ?: throw ContentNotFoundException

        if (principal.facultyId != null && currentHead.facultyId != principal.facultyId) {
            throw AccessDeniedException("You can't delete this head with facultyId: ${currentHead.facultyId}")
        }

        if (headsDao.deleteHead(headId)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.deleteHeads() {
    val headsDao by inject<HeadsDao>()

    delete {
        val principal = call.principal<UserPrincipal>() ?: throw UnknownTokenException
        val headIds = call.request.queryParameters.getIntListOrThrow(
            key = "headIds",
            requiredNotEmpty = true
        )

        val currentHeads = headsDao.allHeadsByIds(headIds)
        if (currentHeads.isEmpty()) {
            throw ContentNotFoundException
        }

        if (principal.facultyId != null &&
            currentHeads.map(Head::facultyId).distinct().singleOrNull() != principal.facultyId
        ) {
            val headsWithoutAccess = currentHeads.filter { head -> head.facultyId != principal.facultyId }.map(Head::id)
            throw AccessDeniedException("You can't delete heads with ids: ${headsWithoutAccess.joinToString()}")
        }

        if (headsDao.deleteHeads(headIds)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}
