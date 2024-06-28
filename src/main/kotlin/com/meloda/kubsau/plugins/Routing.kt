package com.meloda.kubsau.plugins

import com.meloda.kubsau.CONFIG_FOLDER
import com.meloda.kubsau.common.Constants
import com.meloda.kubsau.common.IS_IN_DOCKER
import com.meloda.kubsau.controller.*
import com.meloda.kubsau.model.ServerInfo
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.get

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

private fun Route.routes() {
    val controllers = listOf(
        get<AuthController>(),
        get<DepartmentController>(),
        get<DirectivityController>(),
        get<DisciplineController>(),
        get<EmployeeController>(),
        get<GroupController>(),
        get<HeadController>(),
        get<ProgramController>(),
        get<StudentController>(),
        get<UserController>(),
        get<WorkController>(),
        get<WorkTypeController>()
    )

    controllers.forEach { controller ->
        controller.createRoutes()
    }
}
