@file:Suppress("unused")

package me.xtrm.unlok

import me.xtrm.unlok.dsl.field
import kotlin.test.Test
import kotlin.test.assertEquals

class InnerClassTest {
    @Test
    fun `can access inner classes static fields`() {
        val privateStaticFinalField by ParentClass.InnerClass::class.field<Int>()
        assertEquals(69, privateStaticFinalField)
    }

    @Test
    fun `can access inner classes virtual fields`() {
        val innerClass = ParentClass.InnerClass()
        val privateFinalField by field<Int>(
            "me.xtrm.unlok.ParentClass.InnerClass",
            ownerInstance = innerClass
        )
        assertEquals(42, privateFinalField)
    }
}

class ParentClass {
    class InnerClass {
        companion object {
            @JvmStatic
            private val privateStaticFinalField = 69
        }

        private val privateFinalField = 42
    }
}
