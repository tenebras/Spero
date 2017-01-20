package com.tenebras.spero

import com.tenebras.spero.di.DInjector
import com.tenebras.spero.route.HttpHandler
import com.tenebras.spero.route.Routes
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.ResourceHandler

open class SperoApp(beforeStart: SperoApp.() -> Any) {
    var port: Int = 8080
    val routes: Routes = Routes()
    val httpHandler: HttpHandler by lazy { HttpHandler(routes, injector) }
    var injectorRules: DInjector.() -> Any? = {}
    val injector: DInjector by lazy {
        DInjector(injectorRules)
    }

    init {
        val app = this
        beforeStart()

        injector.append {
            SperoApp::class with { app }
        }

        start()
    }

    fun injection(x: DInjector.() -> Any?) {
        injectorRules = x
    }

    fun init(h: () -> Any): SperoApp {
        h.invoke()
        return this
    }

    fun listen(port: Int): SperoApp {
        this.port = port
        return this
    }

    fun routes(x: Routes.() -> Any): SperoApp {
        httpHandler.routes.add(x)
        return this
    }

    fun start(): SperoApp {

        if (routes.isEmpty()) {
            throw Exception("Can't start the server. Nothing to routes.")
        }

        httpHandler.resourceHandlers.add(ResourceHandler().apply {
            isDirectoriesListed = false
            resourceBase = "src/resources/public"
            welcomeFiles = arrayOf("index.html")
        })

        with(Server(port)) {
            handler = httpHandler
            start()
            join()
        }

        println("Spero server started on http://localhost:$port/")

        return this
    }
}