
import me.xtrm.unlok.accessor.AccessorBuilder
import me.xtrm.unlok.accessor.FieldAccessor
import kotlin.test.Test
import kotlin.test.assertEquals

internal class FieldAccessorBuilderTests {

    @Test
    fun canbuildclass() {
        val accessor: FieldAccessor<String> = AccessorBuilder.fieldAccessor(
            "POJO",
            "NAME"
        )

        assertEquals(accessor.get(), "NamePOJOStatic")
        accessor.set("ChangedName")
        assertEquals(accessor.get(), "ChangedName")
    }

}