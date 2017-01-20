package com.tenebras.spero

import com.github.salomonbrys.kodein.fullDispString
import com.tenebras.spero.db.getUUID
import com.tenebras.spero.db.getZonedDateTime
import com.tenebras.spero.db.getOffsetDateTime
import java.sql.ResultSet
import java.sql.Time
import java.sql.Timestamp
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.jvm.javaType
import kotlin.reflect.primaryConstructor

open class ValueResolver {
    val resolvers = mutableMapOf<String, (ResultSet, String)->Any>()
    val fallbackResolver = {rs: ResultSet, name: String, type: KType ->

        val typeName = type.javaType.typeName

        if (typeName.endsWith("[]")) {
            rs.getArray(name).array
        } else {
            val constructor = Class.forName(typeName).kotlin.primaryConstructor

            if (constructor == null || constructor.parameters.size != 1) {
                throw Exception("No suitable function for $typeName. Constructor with one parameter required.")
            }

            constructor.call(resolve(rs, constructor.parameters.first().type, name))
        }
    }

    init {
        register(String::class, ResultSet::getString)
        register(Byte::class, ResultSet::getByte)
        register(Short::class, ResultSet::getShort)
        register(Int::class, ResultSet::getInt)
        register(Long::class, ResultSet::getLong)
        register(Float::class, ResultSet::getFloat)
        register(Double::class, ResultSet::getDouble)
        register(Boolean::class, ResultSet::getBoolean)
        register(UUID::class, ResultSet::getUUID)
        register(ZonedDateTime::class, ResultSet::getZonedDateTime)
        register(OffsetDateTime::class, ResultSet::getOffsetDateTime)
        register(Date::class, ResultSet::getDate)
        register(Time::class, ResultSet::getTime)
        register(Timestamp::class, ResultSet::getTimestamp)
    }

    fun register (type: KClass<*>, resolver: (ResultSet, String)->Any): ValueResolver {
        resolvers.put(type.qualifiedName!!, resolver)
        return this
    }

    fun resolve(rs: ResultSet, param: KParameter): Any {
        return resolve(rs, param.type, param.name!!)
    }

    fun resolve(rs: ResultSet, type: KType, name: String): Any {
        if (resolvers.contains(type.toString())) {
            return resolvers[type.toString()]!!.invoke(rs, name)
        } else{
            try {
                return fallbackResolver.invoke(rs, name, type)
            } catch (e: Exception) {}
        }

        throw Exception("No resolver for type $type")
    }
}