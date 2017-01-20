package com.spero.route.param

open class RouteParam(
    var name: String = "",
    var value: String = "",
    var validator: (String)->Boolean,
    var converter: Function<Any>? = null
)