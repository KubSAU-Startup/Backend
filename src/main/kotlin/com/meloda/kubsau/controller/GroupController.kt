package com.meloda.kubsau.controller

import com.meloda.kubsau.common.*
import com.meloda.kubsau.model.ContentNotFoundException
import com.meloda.kubsau.model.UnknownException
import com.meloda.kubsau.model.respondSuccess
import com.meloda.kubsau.repository.GroupRepository
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

class GroupController(private val repository: GroupRepository) {

    context(Route)
    private fun routes() {
        authenticate {
            route("/groups") {
                getGroups()
                getGroupById()
                addGroup()
                editGroup()
                deleteGroupById()
                deleteGroupsByIds()
            }
        }
    }

    private fun Route.getGroups() {
        get {
            val principal = call.userPrincipal()
            val groupIds = call.request.queryParameters.getIntList(
                key = "groupIds",
                defaultValue = emptyList()
            )

            val groups = if (groupIds.isEmpty()) {
                repository.getAllGroups(principal.facultyId)
            } else {
                // TODO: 17/06/2024, Danil Nikolaev: check access
                repository.getGroupsByIds(groupIds)
            }

            respondSuccess { groups }
        }
    }

    private fun Route.getGroupById() {
        get("{id}") {
            val groupId = call.parameters.getIntOrThrow("id")
            val group = repository.getGroupById(groupId) ?: throw ContentNotFoundException

            respondSuccess { group }
        }
    }

    private fun Route.addGroup() {
        post {
            val parameters = call.receiveParameters()

            val title = parameters.getStringOrThrow("title")
            val directivityId = parameters.getIntOrThrow("directivityId")

            // TODO: 17/06/2024, Danil Nikolaev: check access ^

            val created = repository.addGroup(
                title = title,
                directivityId = directivityId
            )

            if (created != null) {
                respondSuccess { created }
            } else {
                throw UnknownException
            }
        }
    }

    private fun Route.editGroup() {
        patch("{id}") {
            val groupId = call.parameters.getIntOrThrow("id")
            val currentGroup = repository.getGroupById(groupId) ?: throw ContentNotFoundException

            val parameters = call.receiveParameters()

            val title = parameters["title"]?.trim()
            val directivityId = parameters["directivityId"]?.toIntOrNull()
            // TODO: 17/06/2024, Danil Nikolaev: check access ^

            repository.editGroup(
                groupId = groupId,
                title = title ?: currentGroup.title,
                directivityId = directivityId ?: currentGroup.directivityId
            ).let { success ->
                if (success) {
                    respondSuccess { 1 }
                } else {
                    throw UnknownException
                }
            }
        }
    }

    private fun Route.deleteGroupById() {
        delete("{id}") {
            val groupId = call.parameters.getIntOrThrow("id")
            // TODO: 17/06/2024, Danil Nikolaev: check access  ^
            if (!repository.isGroupExist(groupId)) {
                throw ContentNotFoundException
            }

            if (repository.deleteGroup(groupId)) {
                respondSuccess { 1 }
            } else {
                throw UnknownException
            }
        }
    }

    private fun Route.deleteGroupsByIds() {
        delete {
            val groupIds = call.request.queryParameters.getIntListOrThrow(
                key = "groupIds",
                requiredNotEmpty = true
            )
            // TODO: 17/06/2024, Danil Nikolaev: check access ^

            val currentGroups = repository.getGroupsByIds(groupIds)
            if (currentGroups.isEmpty()) {
                throw ContentNotFoundException
            }

            if (repository.deleteGroups(groupIds)) {
                respondSuccess { 1 }
            } else {
                throw UnknownException
            }
        }
    }

    companion object {
        context(Route)
        fun routes() {
            val controller by inject<GroupController>()
            controller.routes()
        }
    }
}
