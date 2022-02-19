public class TestThrows {

    public void testThrow() throws IllegalAccessException {
        throw new IllegalAccessException("Cannot set final field {}.{}{}");
    }

}
