package me.xtrm.unlok.delegate

import me.xtrm.unlok.accessor.AccessorBuilder
import me.xtrm.unlok.api.accessor.MethodAccessor
import kotlin.reflect.KProperty

class MethodDelegate<T> (
    private val ownerClass: String,
    private val methodName: String = "",
    private val methodDesc: String = "",
    private val owner: Any? = null,
) {
    private var accessor: MethodAccessor<T>? = null

    init {
        if (methodName.isNotBlank()) {
            this.accessor = AccessorBuilder.methodAccessor(ownerClass, methodName, methodDesc, owner)
        }
    }
    /**
     * @param arguments the method args
     * @return the method's return value
     */
    operator fun invoke(vararg arguments: Any?): T? =
        accessor?.invoke(*arguments)

    operator fun getValue(t: Any?, property: KProperty<*>): MethodDelegate<T> {
        if (accessor == null) {
            val name = methodName.ifBlank { property.name }
            this.accessor = AccessorBuilder.methodAccessor(ownerClass, name, methodDesc, owner)
        }
        return this
    }
}
