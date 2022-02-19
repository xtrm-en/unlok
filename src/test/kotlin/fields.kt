import me.xtrm.unlok.dsl.field
import kotlin.test.Test
import kotlin.test.assertEquals

internal class FieldAccessorTests {

    @Test
    fun `can access private static fields`() {
        var name by field<String>(PrivateFieldHolder::class.java, "NAME")

        assertEquals(name, "John")
        name = "Doe"
        assertEquals(name, "Doe")

        assertEquals(PrivateFieldHolder.nameGetter(), "Doe")
    }

    @Test
    fun `can access private virtual fields`() {
        val holder = PrivateFieldHolder("Jhonny")
        assertEquals(holder.surnameGetter(), "Jhonny")

        var surname by field<String>(PrivateFieldHolder::class.java, "surname", holder)
        surname = "Doey"

        assertEquals(surname, "Doey")
        assertEquals(holder.surnameGetter(), "Doey")
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