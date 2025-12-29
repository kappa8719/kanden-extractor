package kr.entropi.kanden.extractor

import java.io.Serializable

class ClassComparator : Comparator<Class<*>>, Serializable {
    override fun compare(c1: Class<*>, c2: Class<*>): Int {
        return c1.simpleName.compareTo(c2.simpleName)
    }

    override fun equals(other: Any?): Boolean {
        return other is ClassComparator
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}