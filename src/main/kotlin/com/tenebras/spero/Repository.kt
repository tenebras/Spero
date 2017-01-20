package com.tenebras.spero

import com.github.salomonbrys.kodein.simpleDispString
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaType

open class Repository<out T>(
    val factory: KFunction<T>,
    val connectionManager: DbConnectionManager,
    val valueResolver: ValueResolver = ValueResolver()
) {

    var tableName = factory.returnType.javaType.simpleDispString.toLowerCase()
    val nextStatementProperties = mutableListOf<Any?>()

    fun all() = "select * from $tableName".queryFor().entities()


    fun bind(param: Any): String {

        var placeholders: String = "?"

        if(param is List<*>) {
            param.forEach {nextStatementProperties.add(it)}
            placeholders = Array(param.size, {'?'}).joinToString(", ")
        } else {
            nextStatementProperties.add(param)
        }

        return placeholders
    }

    fun String.queryFor(): ResultSet {
        val stmt = connectionManager.connection().prepareStatement(this)

        nextStatementProperties.forEachIndexed { i, value -> stmt.setObject(i+1, value) }
        nextStatementProperties.clear()

        return stmt.executeQuery()
    }

    fun String.execute(): Boolean {
        val stmt = connectionManager.connection().prepareStatement(this)

        nextStatementProperties.forEachIndexed { i, value -> stmt.setObject(i+1, value) }
        nextStatementProperties.clear()

        return stmt.execute()
    }


    fun String.prepare(): PreparedStatement = connectionManager.connection().prepareStatement(this)

    infix fun String.bind(param: Any): PreparedStatement {

        val stmt: PreparedStatement
        val regexp = Regex(":([\\w]+)")

        if(param is List<*>) {
            val placeholder = if(param is List<*>) Array(param.size, {'?'}).joinToString(", ") else "?"
            stmt = connectionManager.connection().prepareStatement(replace(regexp, placeholder))
            param.forEachIndexed { i, value -> stmt.setObject(i+1, value)}
        } else {
            stmt = connectionManager.connection().prepareStatement(replace(regexp, "?"))
            stmt.setObject(1, param)
        }

        return stmt
    }

    infix fun String.bind(params: Map<String, Any>): PreparedStatement {

        val lists = mutableListOf<String>()
        var sql = this

        for((key, value) in params) {
            if (value is List<*>) {
                lists.add(key)
                sql = sql.replace(":$key", Array( value.size, {'?'} ).joinToString(", "))
            }
        }

        val regexp = Regex(":([\\w]+)")
        val stmt = connectionManager.connection().prepareStatement(sql.replace(regexp, "?"))

        var i = 1
        regexp.findAll(this).forEach {
            val name = it.groupValues[1]

            if (lists.contains(name)) {
                (params[name] as List<*>).forEach {stmt.setObject(i++, it)}
            } else {
                stmt.setObject(i++, params[name])
            }
        }

        return stmt
    }

    fun ResultSet.entities(): List<T> {
        val result = mutableListOf<T>()

        while (this.next()) {
            result.add(this.entity())
        }

        return result
    }

    fun ResultSet.entity(): T {
        val params = mutableListOf<Any>()
        factory.parameters.forEach { params.add(valueResolver.resolve(this, it)) }

        return factory.call(*params.toTypedArray())
    }

    inline fun <reified T> ResultSet.singleValue(): T {
        next()
        return getObject(1) as T
    }

    fun PreparedStatement.entities(): List<T> = executeQuery().entities()
    fun PreparedStatement.entity(): T = executeQuery().entity()
    inline fun <reified T> PreparedStatement.singleValue(): T = executeQuery().singleValue()
}