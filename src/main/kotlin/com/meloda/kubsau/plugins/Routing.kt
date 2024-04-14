package com.meloda.kubsau.plugins

import com.meloda.kubsau.common.Constants
import com.meloda.kubsau.route.account.account
import com.meloda.kubsau.route.auth.auth
import com.meloda.kubsau.route.department.departments
import com.meloda.kubsau.route.disciplines.disciplines
import com.meloda.kubsau.route.groups.groups
import com.meloda.kubsau.route.journal.journals
import com.meloda.kubsau.route.majors.majors
import com.meloda.kubsau.route.programs.programs
import com.meloda.kubsau.route.qr.qr
import com.meloda.kubsau.route.specializations.specializations
import com.meloda.kubsau.route.students.students
import com.meloda.kubsau.route.teachers.teachers
import com.meloda.kubsau.route.users.users
import com.meloda.kubsau.route.works.works
import com.meloda.kubsau.route.worktypes.workTypes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.routing() {
    routing {
        get("/") {
            call.respondText {
                "Server is working.\nVersion: ${Constants.BACKEND_VERSION}"
            }
        }

        auth()
        account()
        journals()
        departments()
        qr()
        disciplines()
        students()
        workTypes()
        programs()
        groups()
        works()
        users()
        teachers()
        specializations()
        majors()
    }
}
