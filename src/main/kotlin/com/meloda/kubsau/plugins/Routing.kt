package com.meloda.kubsau.plugins

import com.meloda.kubsau.CONFIG_FOLDER
import com.meloda.kubsau.common.Constants
import com.meloda.kubsau.common.IS_IN_DOCKER
import com.meloda.kubsau.route.account.accountRoutes
import com.meloda.kubsau.route.auth.authRoutes
import com.meloda.kubsau.route.department.departmentsRoutes
import com.meloda.kubsau.route.directivities.directivitiesRoutes
import com.meloda.kubsau.route.disciplines.disciplinesRoutes
import com.meloda.kubsau.route.employees.employeesRoutes
import com.meloda.kubsau.route.grades.gradesRoutes
import com.meloda.kubsau.route.groups.groupsRoutes
import com.meloda.kubsau.route.heads.majorsRoutes
import com.meloda.kubsau.route.programs.programsRoutes
import com.meloda.kubsau.route.qr.qrRoutes
import com.meloda.kubsau.route.students.studentsRoutes
import com.meloda.kubsau.route.users.usersRoutes
import com.meloda.kubsau.route.works.worksRoutes
import com.meloda.kubsau.route.worktypes.workTypesRoutes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.routing() {
    routing {
        get("/") {
            call.respond(
                status = HttpStatusCode.OK,
                message = ServerInfo(
                    version = Constants.BACKEND_VERSION,
                )
            )
        }

        swaggerUI(
            path = "/api/docs",
            swaggerFile = if (IS_IN_DOCKER) "$CONFIG_FOLDER/docs/openapi.yml" else "docs/openapi.yml"
        )

        authRoutes()
        accountRoutes()
        departmentsRoutes()
        qrRoutes()
        disciplinesRoutes()
        studentsRoutes()
        workTypesRoutes()
        programsRoutes()
        groupsRoutes()
        worksRoutes()
        usersRoutes()
        employeesRoutes()
        directivitiesRoutes()
        majorsRoutes()
        gradesRoutes()
    }
}

private data class ServerInfo(val version: String)
