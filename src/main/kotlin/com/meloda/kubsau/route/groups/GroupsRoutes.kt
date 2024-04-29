package com.meloda.kubsau.route.groups

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.database.groups.GroupsDao
import com.meloda.kubsau.errors.ContentNotFoundException
import com.meloda.kubsau.errors.UnknownException
import com.meloda.kubsau.errors.ValidationException
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.groupsRoutes() {
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
    val groupsDao by inject<GroupsDao>()

    get {
        val groupIds = call.request.queryParameters["groupIds"]
            ?.split(",")
            ?.map(String::trim)
            ?.mapNotNull(String::toIntOrNull)
            ?: emptyList()

        val groups = if (groupIds.isEmpty()) {
            groupsDao.allGroups()
        } else {
            groupsDao.allGroupsByIds(groupIds)
        }

        respondSuccess { groups }
    }
}

private fun Route.getGroupById() {
    val groupsDao by inject<GroupsDao>()

    get("{id}") {
        val groupId = call.parameters["id"]?.toIntOrNull() ?: throw ValidationException("id is empty")
        val group = groupsDao.singleGroup(groupId) ?: throw ContentNotFoundException

        respondSuccess { group }
    }
}

private fun Route.addGroup() {
    val groupsDao by inject<GroupsDao>()

    post {
        val parameters = call.receiveParameters()

        val title = parameters["title"]?.trim() ?: throw ValidationException("title is empty")
        val directivityId = parameters["directivityId"]?.toIntOrNull() ?: throw ValidationException("directivityId is empty")

        val created = groupsDao.addNewGroup(
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
    val groupsDao by inject<GroupsDao>()

    patch("{id}") {
        val groupId = call.parameters["id"]?.toIntOrNull() ?: throw ValidationException("id is empty")
        val currentGroup = groupsDao.singleGroup(groupId) ?: throw ContentNotFoundException

        val parameters = call.receiveParameters()

        val title = parameters["title"]?.trim()
        val directivityId = parameters["directivityId"]?.toIntOrNull()

        groupsDao.updateGroup(
            groupId = groupId,
            title = title ?: currentGroup.title,
            directivityId = directivityId ?: currentGroup.directivityId
        ).let { changedCount ->
            if (changedCount == 1) {
                respondSuccess { 1 }
            } else {
                throw UnknownException
            }
        }
    }
}

private fun Route.deleteGroupById() {
    val groupsDao by inject<GroupsDao>()

    delete("{id}") {
        val groupId = call.parameters["id"]?.toIntOrNull() ?: throw ValidationException("id is empty")
        groupsDao.singleGroup(groupId) ?: throw ContentNotFoundException

        if (groupsDao.deleteGroup(groupId)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}

private fun Route.deleteGroupsByIds() {
    val groupsDao by inject<GroupsDao>()

    delete {
        val groupIds = call.request.queryParameters["groupIds"]
            ?.split(",")
            ?.map(String::trim)
            ?.mapNotNull(String::toIntOrNull)
            ?: throw ValidationException("groupIds is empty")

        val currentGroups = groupsDao.allGroupsByIds(groupIds)
        if (currentGroups.isEmpty()) {
            throw ContentNotFoundException
        }

        if (groupsDao.deleteGroups(groupIds)) {
            respondSuccess { 1 }
        } else {
            throw UnknownException
        }
    }
}
