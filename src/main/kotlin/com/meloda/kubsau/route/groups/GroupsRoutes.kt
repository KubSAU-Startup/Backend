package com.meloda.kubsau.route.groups

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.database.groups.GroupsDao
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.groupsRoutes() {
    authenticate {
        route("/groups") {
            getAllGroups()
        }
    }
}

private fun Route.getAllGroups() {
    val groupsDao by inject<GroupsDao>()

    get {
        val groups = groupsDao.allGroups()

        respondSuccess { groups }
    }
}
