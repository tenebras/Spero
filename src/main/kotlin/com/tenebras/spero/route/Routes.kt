package com.tenebras.spero.route

import kotlin.reflect.KFunction

class Routes  {//(initializer: Routes.()->Route = {})
    val routes: MutableList<Route> = mutableListOf()
    // init { add(initializer) }

    /*
//    infix fun String.by(x: (request: Request)->Response): Route {
//        val route = Route.fromString(this, x)
//        routes.add(route)
//        return route
//    }

//    infix fun List<String>.by(action: (request: Request)->Response): Route {
//
//        val route = Route(this, action)
//        routes.add(route)
//        return route
//    }
    */

//    infix fun String.by(x: KFunction<String>): Route {
//        val route = Route.fromString(this, x)
//        routes.add(route)
//        return route
//    }

//    infix fun String.with(x: KFunction<String>): Route {
//        val route = Route.fromString(this, x)
//        routes.add(route)
//        return route
//    }

    infix fun String.with(x: KFunction<Any>): Route {
        val route = Route.fromString(this, x)
        routes.add(route)
        return route
    }

    fun group(prefix: String, initializer: Routes.() -> Any): Route {
        return Route(emptyList(), "", ::String)
    }

    fun group(initializer: Routes.() -> Any): Route {
        return Route(emptyList(), "", ::String)
    }

    fun add(route: Route) = routes.add(route)
    fun add(initializer: Routes.()->Any) = initializer.invoke(this)

    fun isEmpty(): Boolean = routes.size == 0

    fun find(method: String, uri: String): Route {

        for(route in routes) {
            if(route.isSatisfied(method, uri)) {
                return route
            }
        }

        throw Exception("Route not found")
    }
}