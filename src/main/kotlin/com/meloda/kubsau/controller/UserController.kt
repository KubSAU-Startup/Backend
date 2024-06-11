package com.meloda.kubsau.controller

import com.meloda.kubsau.common.getIntList
import com.meloda.kubsau.common.getIntOrThrow
import com.meloda.kubsau.common.getStringOrThrow
import com.meloda.kubsau.model.*
import com.meloda.kubsau.plugins.UserPrincipal
import com.meloda.kubsau.service.UserService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

class UserController(private val userService: UserService) {

    context(Route)
    private fun routes() {
        authenticate {
            route("/users") {
                getUsers()
                getUserById()
            }

            route("/account") {
                getAccountInfo()
                editAccountInfo()
            }
        }
    }

    private fun Route.getUsers() {
        get {
            val userIds = call.request.queryParameters.getIntList(
                key = "userIds",
                defaultValue = emptyList()
            )

            val users = if (userIds.isEmpty()) {
                userService.getAllUsers()
            } else {
                userService.getUsersByIds(userIds)
            }

            respondSuccess { users }
        }
    }

    private fun Route.getUserById() {
        get("{id}") {
            val userId = call.parameters.getIntOrThrow("id")
            val user = userService.getUserById(userId) ?: throw ContentNotFoundException

            respondSuccess { user }
        }
    }

    private fun Route.getAccountInfo() {
        get {
            val principal = call.principal<UserPrincipal>() ?: throw UnknownTokenException
            val info = userService.getAccountInfo(principal)
            respondSuccess { info }
        }
    }

    private fun Route.editAccountInfo() {
        patch {
            val principal = call.principal<UserPrincipal>() ?: throw UnknownTokenException

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

    companion object {
        context(Route)
        fun routes() {
            val controller by inject<UserController>()
            controller.routes()
        }
    }
}
