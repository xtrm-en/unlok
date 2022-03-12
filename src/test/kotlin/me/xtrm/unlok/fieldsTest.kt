package me.xtrm.unlok

import me.xtrm.unlok.dsl.field
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("LocalVariableName")
class FieldAccessorTests {
    @Test
    fun `can access private static fields`() {
        var staticName by PrivateFieldHolder::class.field<String>()

        assertEquals(staticName, "John")
        assertDoesNotThrow { staticName = "Doe" }
        assertEquals(staticName, "Doe")

        assertEquals(PrivateFieldHolder.getName(), "Doe")
    }

    @Test
    fun `can access private virtual fields`() {
        val holder = PrivateFieldHolder("Richard")

        var surname by PrivateFieldHolder::class.field<String>(
            ownerInstance = holder
        )
        surname = "Roe"

        assertEquals(surname, "Roe")
        assertEquals(holder.getSurname(), "Roe")
    }

    @Test
    fun `can access final fields`() {
        val holder = FinalFieldHolder("John")
        var name by FinalFieldHolder::class.field<String>(
            ownerInstance = holder
        )

        assertEquals(name, "John")
        assertDoesNotThrow { name = "Other Name" }
        assertEquals(name, "Other Name")
    }
}

class PrivateFieldHolder(
    private var surname: String
) {
    companion object {
        @JvmStatic
        private var staticName = "John"

        fun getName() = staticName
    }

    fun getSurname() = surname
}

@Suppress("unused")
class FinalFieldHolder(
    private val name: String
)
