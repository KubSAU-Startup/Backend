package com.meloda.kubsau.plugins

import com.meloda.kubsau.CONFIG_FOLDER
import com.meloda.kubsau.common.Constants
import com.meloda.kubsau.common.IS_IN_DOCKER
import com.meloda.kubsau.controller.GroupController
import com.meloda.kubsau.controller.UserController
import com.meloda.kubsau.controller.WorkController
import com.meloda.kubsau.model.ServerInfo
import com.meloda.kubsau.route.auth.authRoutes
import com.meloda.kubsau.route.department.departmentsRoutes
import com.meloda.kubsau.route.directivities.directivitiesRoutes
import com.meloda.kubsau.route.disciplines.disciplinesRoutes
import com.meloda.kubsau.route.employees.employeesRoutes
import com.meloda.kubsau.route.grades.gradesRoutes
import com.meloda.kubsau.route.heads.majorsRoutes
import com.meloda.kubsau.route.programs.programsRoutes
import com.meloda.kubsau.route.qr.qrRoutes
import com.meloda.kubsau.route.students.studentsRoutes
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

        routes()
    }
}

context(Route)
private fun routes() {
    authRoutes()
    departmentsRoutes()
    qrRoutes()
    disciplinesRoutes()
    studentsRoutes()
    workTypesRoutes()
    programsRoutes()
    GroupController.routes()
    WorkController.routes()
    UserController.routes()
    employeesRoutes()
    directivitiesRoutes()
    majorsRoutes()
    gradesRoutes()
}
