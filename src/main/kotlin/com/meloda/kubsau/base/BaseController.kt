package com.meloda.kubsau.base

import io.ktor.server.routing.*

interface BaseController {

    fun Route.routes()

    context(Route)
    fun createRoutes() = routes()
}
