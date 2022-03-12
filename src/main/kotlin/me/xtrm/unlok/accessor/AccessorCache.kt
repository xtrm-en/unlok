package me.xtrm.unlok.accessor

import me.xtrm.unlok.api.accessor.FieldAccessor
import me.xtrm.unlok.api.accessor.MethodAccessor
import org.objectweb.asm.tree.ClassNode
import java.util.WeakHashMap

/**
 * Caches fields and methods using corresponding hash values.
 * @see [AccessorBuilder]
 *
 * @author xtrm
 * @since 0.0.1
 */
data class AccessorCache(
    val fieldVirtualCache: MutableMap<Int, FieldAccessor<*>> =
        WeakHashMap(),

    val fieldStaticCache: MutableMap<Int, FieldAccessor<*>> =
        WeakHashMap(),

    val methodVirtualCache: MutableMap<Int, MethodAccessor<*>> =
        WeakHashMap(),

    val methodStaticCache: MutableMap<Int, MethodAccessor<*>> =
        WeakHashMap(),

    val classCache: MutableMap<String, ClassNode> =
        WeakHashMap(),
)
