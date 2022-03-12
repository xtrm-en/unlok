package me.xtrm.unlok.delegate

import me.xtrm.unlok.accessor.AccessorBuilder
import me.xtrm.unlok.api.accessor.FieldAccessor
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

    init {
        if (this.fieldName.isNotBlank()) {
            this.accessor = AccessorBuilder.fieldAccessor(
                this.ownerClass,
                this.fieldName,
                this.owner
            )
        }
    }

    operator fun getValue(t: T?, property: KProperty<*>): T? =
        this.ensureAccessor(property).get()

    operator fun setValue(t: T?, property: KProperty<*>, value: T?): Unit =
        this.ensureAccessor(property).set(value)

    private fun ensureAccessor(property: KProperty<*>): FieldAccessor<T> =
        this.run {
            if (this.accessor == null) {
                this.accessor = AccessorBuilder.fieldAccessor(
                    this.ownerClass,
                    this.fieldName.ifBlank { property.name },
                    this.owner
                )
            }

            this.accessor!!
        }
}
