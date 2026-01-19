package kr.entropi.kanden.extractor

import net.minecraft.resources.ResourceKey

object RegistryKeyComparator : Comparator<ResourceKey<*>> {
    override fun compare(o1: ResourceKey<*>, o2: ResourceKey<*>): Int {
        val c1 = o1.registry().compareTo(o2.registry())

        if (0 != c1) {
            return c1
        }

        return o1.identifier().compareTo(o2.identifier())
    }
}