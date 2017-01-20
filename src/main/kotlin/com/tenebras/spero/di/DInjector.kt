package com.tenebras.spero.di

import com.github.salomonbrys.kodein.TypeReference
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.primaryConstructor

class Creator {
    val typeMap = mutableMapOf<String, Resolver<Any>>()

    @Suppress("UNCHECKED_CAST")
    fun <T: Any> create(typeName: String): T {

        if(canCreate(typeName)) {
            println("Can create $typeName")
        } else {
            println("Can't create $typeName !!!")
        }

        if (typeMap.containsKey(typeName)) {
            return typeMap[typeName]!!.get(this) as T
        } else {
            // Create not registered instance
            println("CAN'T CREATE INSTANCE OF $typeName!!!")
        }

        throw IllegalArgumentException("Can't create instance of $typeName")
    }

    inline fun register(typeName: String, constructor: Resolver<Any>) {
        typeMap.put(typeName, constructor)
    }

    fun canCreate(typeName: String): Boolean {

        if(typeName.startsWith("kotlin.")) {
            return false
        }

        if(typeMap.containsKey(typeName)) {
            typeMap[typeName]!!.getDependencies().forEach {
                if(!canCreate(it.typeName) && !it.isOptional) {
                    println("Can't create dependency of $typeName. Dependency is $it")
                    return false
                }
            }
        } else {
            try {

                // Auto find class with it's constructor
                val constructor = Class.forName(typeName).kotlin.primaryConstructor

                if(constructor == null) {
                    println("Can't find primary constructor for $typeName")
                    return false
                }

                // Register constructor for future use
                register(typeName, Constructor(constructor))
                return canCreate(typeName)

            } catch(e: ClassNotFoundException) {
                println("Class $typeName not found")
                return false
            }
        }

        return true
    }
}

class Dependency(val name: String, val typeName: String, val isOptional: Boolean, val param: KParameter)

interface Resolver<T> {
    fun getDependencies(): List<Dependency>
    fun get(creator: Creator): T
}

class Instance<T>(private val instance: T): Resolver<T> {
    override fun getDependencies(): List<Dependency> = emptyList()
    override fun get(creator: Creator): T = instance
}

class Constructor<T>(val function: KFunction<T>): Resolver<T> {
    val dependsOn = mutableListOf<Dependency>()

    init {
        function.parameters.forEach {
            dependsOn.add(Dependency(it.name!!, it.type.toString(), it.isOptional, it))
        }
    }

    override fun getDependencies() = dependsOn
    override fun get(creator: Creator): T {
        return function.callBy(prepareParams(creator))
    }

    fun prepareParams(creator: Creator): HashMap<KParameter, Any?> {
        val params = hashMapOf<KParameter, Any?>()

        getDependencies().forEach {
            if(creator.canCreate(it.typeName)) {
                params.put(it.param, creator.create(it.typeName))
            } else if(!it.isOptional){
                throw IllegalStateException("Can't prepare params for ${function.returnType}. Dependency is ${it.typeName}")
            }
        }

        return params
    }
}

class DInjector(x: DInjector.()->Any?) {

    val creator = Creator()

    init { x() }

    fun append(initializer: DInjector.()->Any?) {
        initializer()
    }

    infix fun <T: Any> KClass<T>.with(constructor: KFunction<T>) {
        creator.register(qualifiedName!!, Constructor(constructor))
    }

    infix fun <T: Any> KClass<T>.with(instance: ()->T) {
        creator.register(qualifiedName!!, Instance(instance.invoke()))
    }

    inline fun <reified T: Any> instance(): T {
        val typeName = (object : TypeReference<T>() {}).type.typeName

        return creator.create(typeName)
    }

    inline fun <reified T: Any> instance(typeName: String): T {
        return creator.create(typeName)
    }
}


//class DInjector(x: DInjector.()->Any?) {
//    val typeMap = mutableMapOf<String, Binder<Any>>()
//
//    init {
//        x.invoke(this)
//    }
//
//    infix fun <T: Any> KClass<T>.with(r: KFunction<T>){
//        typeMap.put(
//                qualifiedName!!,
//                Binder(Factory {
//                    r.call()
//                }))
//    }
//
//    infix fun <T: Any> KClass<T>.with(r: T) {
//        typeMap.put(
//                qualifiedName!!,
//                Binder(Factory { r })
//        )
//    }
//
//    infix fun <T: Any> KClass<T>.with(r: Factory<T>)  = typeMap.put(qualifiedName!!, Binder(r))
//
//    inline fun <reified T: Any> bind() = Binder<T>()
//    inline fun bind(x: Any) {
//        typeMap.put( x.javaClass.typeName, Binder(Factory {x}) )
//    }
//
//    inline fun <reified T: Any> instance(): T {
//        val typeName = (object : TypeReference<T>() {}).type.typeName
//
//        if (typeMap.containsKey(typeName)) {
//            return typeMap[typeName]!!.factory!!.creator.invoke() as T
//        }
//
//        throw IllegalArgumentException("Can't create instance of $typeName")
//    }
//
////    inline fun <T: Any> single(noinline x: ()->T) = Factory(x)
////    inline fun <T: Any> provider(noinline x: ()->T) = Factory(x)
//}
//
//class Factory<out T: Any>(val creator: ()->T)
//
//class Binder<T: Any>(var factory: Factory<T>? = null) {
//    infix fun <R: T> with(r: Factory<R>) {}
//    infix fun <R: T> with(r: R) {}
//}