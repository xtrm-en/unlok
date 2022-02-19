package me.xtrm.unlok.delegate

import me.xtrm.unlok.accessor.AccessorBuilder
import me.xtrm.unlok.accessor.FieldAccessor
import kotlin.reflect.KProperty

/**
 * @author xtrm-en
 * @since 0.0.1
 */
class FieldDelegate<T>(
    private val ownerClass: String,
    private val fieldName: String = "",
    private val owner: Any? = null,
) {
    private var accessor: FieldAccessor<T>? = null

    operator fun getValue(t: T?, property: KProperty<*>): T? {
        if(accessor == null) {
            val name = fieldName.ifBlank { property.name }
            this.accessor = AccessorBuilder.fieldAccessor(ownerClass, name, owner)
        }
        return accessor!!.get()
    }

    operator fun setValue(t: T?, property: KProperty<*>, value: T?) {
        if(accessor == null) {
            val name = fieldName.ifBlank { property.name }
            this.accessor = AccessorBuilder.fieldAccessor(ownerClass, name, owner)
        }
        accessor!!.set(value)
    }

}