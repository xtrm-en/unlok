
import me.xtrm.unlok.dsl.field
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("LocalVariableName")
internal class FieldAccessorTests {

    @Test
    fun `can access private static fields`() {
        var NAME by field<String>(PrivateFieldHolder::class)

        assertEquals(NAME, "John")
        NAME = "Doe"
        assertEquals(NAME, "Doe")

        assertEquals(PrivateFieldHolder.nameGetter(), "Doe")
    }

    @Test
    fun `can access private virtual fields`() {
        val holder = PrivateFieldHolder("Jhonny")
        assertEquals(holder.surnameGetter(), "Jhonny")

        var surname by field<String>(PrivateFieldHolder::class, ownerInstance = holder)
        surname = "Doey"

        assertEquals(surname, "Doey")
        assertEquals(holder.surnameGetter(), "Doey")
    }

    @Test
    fun `can access final fields`() {
        val holder = FinalFieldHolder("John")
        var name by field<String>(FinalFieldHolder::class, ownerInstance = holder)

        assertEquals(name, "John")

        assertDoesNotThrow {
            name = "Other Name"
        }

        assertEquals(name, "Other Name")
    }
}

internal class PrivateFieldHolder(
    private var surname: String
) {
    companion object {
        @JvmStatic
        private var NAME = "John"

        fun nameGetter() = NAME
    }

    fun surnameGetter() = surname
}

internal class FinalFieldHolder(
    private val name: String
)
