internal class MethodAccessorTests {

    /* This is still testing syntax
    @Test
    fun `can call private method`() {
        method(PrivateMethodHolder::class.java, "getPrivateStringy", Type.getType("java/lang/String"))
            .invoke<String>()
    }
     */
}

internal class PrivateMethodHolder {
    private fun getPrivateStringy(): String =
        "real private stuff"
}
