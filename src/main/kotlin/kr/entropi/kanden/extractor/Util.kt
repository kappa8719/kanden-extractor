package kr.entropi.kanden.extractor

import com.google.gson.JsonNull
import com.google.gson.JsonPrimitive
import sun.reflect.ReflectionFactory
import java.util.Optional

fun <T> initiateWithoutConstructor(clazz: Class<T>): T {
    val factory = ReflectionFactory.getReflectionFactory()
    try {
        val objectConstructor = java.lang.Object::class.java.getDeclaredConstructor()
        val constructor = factory.newConstructorForSerialization(clazz, objectConstructor)
        return clazz.cast(constructor.newInstance())!!
    } catch (e: Throwable) {
        throw IllegalArgumentException("Failed to initiate ${clazz.name} without constructor", e)
    }
}

fun jsonNullable(string: String?) =
    string?.let { JsonPrimitive(string) } ?: JsonNull.INSTANCE

fun String.pascalCase(): String {
    val result = StringBuilder()
    var capitalizeNext = true

    for (c in this) {
        if (Character.isWhitespace(c) || '_' == c) {
            capitalizeNext = true
        } else if (capitalizeNext) {
            result.append(c.uppercaseChar())
            capitalizeNext = false
        } else {
            result.append(c.lowercaseChar())
        }
    }

    return result.toString()
}