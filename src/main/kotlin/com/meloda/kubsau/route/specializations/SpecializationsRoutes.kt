package com.meloda.kubsau.route.specializations

import com.meloda.kubsau.api.respondSuccess
import com.meloda.kubsau.database.specializations.SpecializationsDao
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.specializations() {
    authenticate {
        route("/specializations") {
            getAllSpecializations()
        }
    }
}

private fun Route.getAllSpecializations() {
    val specializationsDao by inject<SpecializationsDao>()

    get {
        val specializations = specializationsDao.allSpecializations()

        respondSuccess { specializations }
    }
}
