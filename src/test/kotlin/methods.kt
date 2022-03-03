import me.xtrm.unlok.delegate.MethodDelegate
import kotlin.test.Test

@Suppress("LocalVariableName")
internal class MethodAccessorTests {

    @Test
    fun `can call private method`() {
        val method by MethodDelegate<String>(MethodAccessorBuilder)
    }
}
