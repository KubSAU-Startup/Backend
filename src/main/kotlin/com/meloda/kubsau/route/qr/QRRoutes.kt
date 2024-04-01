package com.meloda.kubsau.route.qr

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.database.groups.GroupsDao
import com.meloda.kubsau.database.programs.ProgramsDao
import com.meloda.kubsau.model.Group
import com.meloda.kubsau.model.Program
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.qr() {
    authenticate {
        route("/qr") {
            getData()
        }
    }
}

private fun Route.getData() {
    val programsDao by inject<ProgramsDao>()
    val groupsDao by inject<GroupsDao>()

    get {
        val programs = programsDao.allPrograms()
        val groups = groupsDao.allGroups()

        respondSuccess { GetDataResponse(programs = programs, groups = groups) }
    }
}

private data class GetDataResponse(
    val programs: List<Program>,
    val groups: List<Group>
)
