package com.tenebras.spero.route

import com.spero.route.param.RouteParam
import kotlin.reflect.KFunction

class Route(val methods: List<String>, url: String, val action: KFunction<Any>) {
    val pattern: String
    var params: MutableMap<String, RouteParam> = mutableMapOf()
    var paramIndexMap: MutableMap<Int, String> = mutableMapOf()
    val after: MutableList<()->Any> = mutableListOf()
    val before: MutableList<()->Any> = mutableListOf()

    companion object {
        fun fromString(uri: String, x: KFunction<Any>): Route {
            val methods: List<String>
            val url: String

            if (uri.contains(' ')) {
                val tmp = uri.split(' ')

                methods = if (tmp[0].contains(',')) tmp[0].split(',') else listOf(tmp[0])
                url = tmp[1]
            } else {
                methods = emptyList()
                url = uri
            }

            return Route(methods, url, x)
        }
    }

    init {
        var pattern = url.replace("/", "\\/")

        if (url.contains('{')) { // URL has parameters

            Regex("\\{.+?\\}").findAll(url).forEach {

                val name: String

                if (it.value.contains(':')) { // has validation regexp
                    val splitterPosition = it.value.indexOf(':')

                    if (it.value.substring(1, splitterPosition).isBlank()) {
                        name = params.size.toString()
                    } else {
                        name = it.value.substring(1, splitterPosition)
                    }

                    val regexp = it.value.substring(splitterPosition + 1, it.value.length - 1)

                    paramIndexMap.put(params.size, name)
                    params.put(name, RouteParam(name, "", {Regex(regexp).matches(it)}))
                    pattern = pattern.replaceFirst(it.value, "($regexp)")

                } else {
                    name = it.value.substring(1, it.value.length-1)
                    paramIndexMap.put(params.size, name)
                    params.put(name, RouteParam(name, "", { Regex(".+").matches(it) }))
                    pattern = pattern.replaceFirst(it.value, "(.+)")
                }

                println(it.value)
            }
        }

        this.pattern = pattern
    }

    fun isSatisfied(method: String, uri: String): Boolean {

        val reg = Regex(pattern)

        if (hasMethod(method) && reg.matches(uri)) {

            val match = reg.findAll(uri).first()

            for ((k, v) in paramIndexMap) {
                println("$v = ${match.groupValues[k + 1]}")
                params[v]?.value = match.groupValues[k + 1]
            }

            return true
        }

        return false
    }

    fun paramByIdx(idx: Int): RouteParam {
        if (idx >= params.size || idx < 0) {
            throw ArrayIndexOutOfBoundsException("No param by index $idx")
        }

        return params[ paramIndexMap[idx] ]!!
    }

    fun hasMethod(method: String): Boolean {
        return methods.isEmpty() || methods.contains(method.toUpperCase())
    }

    fun convert(param: String, x: (value: String)-> Any): Route {
        return this
    }

    fun convert(param: String, x: Function<Any>): Route {
        //action.reflect().parameters[0].type.toString()
        return this
    }

    fun after(x: ()->Any): Route {
        after.add(x)
        return this
    }

    fun before(x: ()->Any): Route {
        before.add(x)
        return this
    }
}