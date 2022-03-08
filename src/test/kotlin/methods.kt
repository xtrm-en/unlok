
import me.xtrm.unlok.dsl.method
import kotlin.test.Test
import kotlin.test.assertTrue

@Suppress("LocalVariableName")
internal class MethodAccessorTests {

    @Test
    fun `can call private static method`() {
        val eq by method<Boolean>(PrivateMethodHolder::class)
        assertTrue { eq("test", "test") ?: false }
        assertTrue { !(eq("test1", "test2") ?: false) }
    }

    @Test
    fun `can call private virtual method`() {
        val instance = PrivateVirtualMethodHolder(true)
        val method by method<Boolean>(PrivateVirtualMethodHolder::class, ownerInstance = instance)
        assertTrue { method() ?: false }
    }
}

internal object PrivateMethodHolder {
    @JvmStatic
    private fun eq(first: String, second: String): Boolean {
        return first == second
    }
}

internal class PrivateVirtualMethodHolder(private val returnValue: Boolean) {
    fun method(): Boolean = returnValue
}
