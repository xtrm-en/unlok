@file:Suppress("unused")

package me.xtrm.unlok

import me.xtrm.unlok.dsl.method
import kotlin.test.Test
import kotlin.test.assertTrue

class MethodAccessorTests {
    @Test
    fun `can call private static method`() {
        val eq by PrivateMethodHolder::class.method<Boolean>()
        assertTrue { eq("test", "test") ?: false }
        assertTrue { !(eq("test1", "test2") ?: false) }
    }

    @Test
    fun `can call private virtual method`() {
        val instance = PrivateVirtualMethodHolder(true)
        val method by PrivateVirtualMethodHolder::class.method<Boolean>(
            ownerInstance = instance
        )

        assertTrue { method() ?: false }
    }
}

object PrivateMethodHolder {
    @JvmStatic
    private fun eq(first: String, second: String): Boolean {
        return first == second
    }
}

class PrivateVirtualMethodHolder(
    private val returnValue: Boolean
) {
    fun method(): Boolean =
        returnValue
}
