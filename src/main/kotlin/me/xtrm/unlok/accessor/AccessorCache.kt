package me.xtrm.unlok.accessor

import me.xtrm.unlok.api.accessor.FieldAccessor
import me.xtrm.unlok.api.accessor.MethodAccessor

data class AccessorCache(
    val fieldVirtualCache: MutableMap<Int, FieldAccessor<*>> = mutableMapOf(),
    val fieldStaticCache: MutableMap<Int, FieldAccessor<*>> = mutableMapOf(),
    val methodVirtualCache: MutableMap<Int, MethodAccessor<*>> = mutableMapOf(),
    val methodStaticCache: MutableMap<Int, MethodAccessor<*>> = mutableMapOf(),
)
