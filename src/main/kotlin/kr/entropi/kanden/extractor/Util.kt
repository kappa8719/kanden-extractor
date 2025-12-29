package kr.entropi.kanden.extractor

import com.google.gson.JsonNull
import com.google.gson.JsonPrimitive
import kr.entropi.kanden.extractor.dummy.DummyPlayerEntity
import sun.reflect.ReflectionFactory

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