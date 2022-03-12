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
    /**
     * The virtual field cache, with a hash of:
     * - the owner [ClassNode] ;
     * - the current field's name ;
     * - the current field's description ;
     * - the current field's signature
     * as a key (using [java.util.Objects.hash]), and a [FieldAccessor] as its
     * value.
     */
    val fieldVirtualCache: MutableMap<Int, FieldAccessor<*>> =
        WeakHashMap(),

    /**
     * The static field cache, with a hash of:
     * - the owner [ClassNode] ;
     * - the current field's name ;
     * - the current field's description ;
     * - the current field's signature
     * as a key (using [java.util.Objects.hash]), and a [FieldAccessor] as its
     * value.
     */
    val fieldStaticCache: MutableMap<Int, FieldAccessor<*>> =
        WeakHashMap(),

    /**
     * The virtual method cache, with a hash of:
     * - the owner [ClassNode] ;
     * - the current method's name ;
     * - the current method's description ;
     * - the current method's signature
     * as a key (using [java.util.Objects.hash]), and a [MethodAccessor] as its
     * value.
     */
    val methodVirtualCache: MutableMap<Int, MethodAccessor<*>> =
        WeakHashMap(),

    /**
     * The static method cache, with a hash of:
     * - the owner [ClassNode] ;
     * - the current method's name ;
     * - the current method's description ;
     * - the current method's signature
     * as a key (using [java.util.Objects.hash]), and a [MethodAccessor] as its
     * value.
     */
    val methodStaticCache: MutableMap<Int, MethodAccessor<*>> =
        WeakHashMap(),

    /**
     * The [ClassNode] cache, with the class' name as a key, and its
     * [ClassNode] as its value.
     */
    val classCache: MutableMap<String, ClassNode> =
        WeakHashMap(),
)
