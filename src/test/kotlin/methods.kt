import me.xtrm.unlok.dsl.method
import kotlin.test.Test

@Suppress("LocalVariableName")
internal class MethodAccessorTests {

    @Test
    fun `can call private method`() {
        val eq by method<Boolean>(PrivateMethodHolder::class)
        assert(eq("test", "test") ?: false)
        assert(!(eq("test1", "test2") ?: false))
    }
}

internal object PrivateMethodHolder {
    @JvmStatic
    private fun eq(first: String, second: String): Boolean {
        return first == second
    }
}
