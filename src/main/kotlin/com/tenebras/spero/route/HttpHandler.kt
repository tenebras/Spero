package com.tenebras.spero.route

import com.tenebras.spero.di.DInjector
import org.eclipse.jetty.server.handler.AbstractHandler
import org.eclipse.jetty.server.handler.ResourceHandler
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.reflect.KType
import kotlin.reflect.jvm.javaType
import kotlin.system.measureTimeMillis

class HttpHandler(val routes: Routes, var injector: DInjector): AbstractHandler() {
    val resourceHandlers = ArrayList<ResourceHandler>()

    override fun handle(target: String?, baseRequest: org.eclipse.jetty.server.Request?, request: HttpServletRequest?, response: HttpServletResponse?) {

        if(response != null && request != null) {
            println(request.queryString)
            println(request.requestURI)
            println(request.requestURL)
            response.contentType = "text/html;charset=utf-8"
            response.status = HttpServletResponse.SC_OK

            try{
                 val execution = measureTimeMillis {
                    val route = routes.find(request.method, request.requestURI)
                    val params: Array<Any> = Array(route.action.parameters.size, {})

                    route.action.parameters.forEachIndexed({
                        idx, param ->

                        if (!param.type.toString().startsWith("kotlin.")) {
                            params[idx] = resolveType(param.type)
                        } else {
                            try {
                                val value = if (route.params.containsKey(param.name))
                                    route.params[param.name]!!.value else route.paramByIdx(idx - 1).value

                                when (param.type.toString()) {
                                    "kotlin.String" -> params[idx] = value
                                    "kotlin.Int" -> params[idx] = value.toInt()
                                    "kotlin.Long" -> params[idx] = value.toLong()
                                    "kotlin.Float" -> params[idx] = value.toFloat()
                                    "kotlin.Double" -> params[idx] = value.toDouble()
                                    "kotlin.Boolean" -> params[idx] = value.toBoolean()
                                    else -> {
                                        params[idx] = value
                                    }
                                }
                            } catch (e: ArrayIndexOutOfBoundsException) {
                                // if(param.isOptional) {}
                                throw e
                            }
                        }
                    })
                    response.writer?.println(route.action.call(*params))
                }

                response.writer?.println("Execution: $execution")
            }   catch (e: Exception) {
                println("Exception: ${e.message}")
                for (resourceHandler in resourceHandlers) {
                    resourceHandler.handle(target, baseRequest, request, response)
                    if (baseRequest!!.isHandled) {
                        //println("$function -- ${request.requestURL}${if (query != null) "?" + query else ""} -- OK @${resourceHandler.resourceBase}")
                        break
                    }
                }

                if(!baseRequest!!.isHandled) {
                    println("ERROR: Can't handle request ${request.queryString}")
                }
            }
        }

        if (baseRequest != null) {
            baseRequest.isHandled = true
        }
    }

    private fun resolveType(type: KType): Any {
        return injector.instance(type.javaType.typeName)
    }
}