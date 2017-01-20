package com.tenebras.spero.route

import kotlin.reflect.KFunction

class Group (val prefix: String) {
    val routes: MutableList<Route> = mutableListOf()

    infix fun String.by(x: KFunction<String>): Route {
        val route = Route.fromString(this, x)
        routes.add(route)
        return route
    }

    fun after(x: ()->Any): Group {
        routes.forEach { it.after(x) }
        return this
    }

    fun before(x: ()->Any): Group {
        routes.forEach { it.before(x) }
        return this
    }
}