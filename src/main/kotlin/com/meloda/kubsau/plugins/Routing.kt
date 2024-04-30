package com.meloda.kubsau.plugins

import com.meloda.kubsau.common.Constants
import com.meloda.kubsau.route.account.accountRoutes
import com.meloda.kubsau.route.auth.authRoutes
import com.meloda.kubsau.route.department.departmentsRoutes
import com.meloda.kubsau.route.directivities.directivitiesRoutes
import com.meloda.kubsau.route.disciplines.disciplinesRoutes
import com.meloda.kubsau.route.employees.employeesRoutes
import com.meloda.kubsau.route.groups.groupsRoutes
import com.meloda.kubsau.route.heads.majorsRoutes
import com.meloda.kubsau.route.programs.programsRoutes
import com.meloda.kubsau.route.qr.qrRoutes
import com.meloda.kubsau.route.students.studentsRoutes
import com.meloda.kubsau.route.users.usersRoutes
import com.meloda.kubsau.route.works.worksRoutes
import com.meloda.kubsau.route.worktypes.workTypesRoutes
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.routing() {
    routing {
        get("/") {
            call.respondText {
                "Server is working.\nVersion: ${Constants.BACKEND_VERSION}"
            }
        }

        authenticate {
            get("/alldata") {
                // TODO: 30/04/2024, Danil Nikolaev: implement
                call.respondText { "Ne rabotaet" }
            }
        }

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
    }
}
