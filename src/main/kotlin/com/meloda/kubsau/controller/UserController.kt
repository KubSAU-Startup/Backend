package com.meloda.kubsau.controller

import com.meloda.kubsau.base.BaseController
import com.meloda.kubsau.common.getStringOrThrow
import com.meloda.kubsau.common.userPrincipal
import com.meloda.kubsau.model.UnknownException
import com.meloda.kubsau.model.respondSuccess
import com.meloda.kubsau.service.UserService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

class UserController(private val userService: UserService) : BaseController {

    override fun Route.routes() {
        authenticate {
            route("/account") {
                getAccountInfo()
                editAccountInfo()
            }
        }
    }

    private fun Route.getAccountInfo() {
        get {
            val principal = call.userPrincipal()
            val info = userService.getAccountInfo(principal)
            respondSuccess { info }
        }
    }

    private fun Route.editAccountInfo() {
        patch {
            val principal = call.userPrincipal()

            val parameters = call.receiveParameters()
            val currentPassword = parameters.getStringOrThrow("currentPassword")
            val newPassword = parameters.getStringOrThrow("newPassword")

            if (userService.updateAccountInfo(principal, currentPassword, newPassword)) {
                respondSuccess { 1 }
            } else {
                throw UnknownException
            }
        }
    }
}
