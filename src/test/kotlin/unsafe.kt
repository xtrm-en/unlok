import me.xtrm.unlok.Unlok.field
import sun.misc.Unsafe
import kotlin.test.Test
import kotlin.test.assertNotNull

internal class UnsafeTests {
    @Test
    fun `can get unsafe`() {
        val unsafe by field<Unsafe>("sun.misc.Unsafe", "theUnsafe")
        assertNotNull(unsafe)
    }
}