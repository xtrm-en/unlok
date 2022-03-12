package me.xtrm.unlok;

import me.xtrm.unlok.delegate.MethodDelegate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("all")
public class JavaMethodsTest {
    @Test
    public void canCallPrivateTypedMethod() {
        MethodDelegate<Boolean> delegate =
            Unlok.<Boolean>method(
                InnerClass.class.getName(),
                "typedFunction",
                "",
                null
            );

        int arg2 = 1;
        Integer arg1 = Integer.valueOf(1);
        String arg3 = "y";
//        Object arg4 = new Object();
        Boolean arg6 = Boolean.FALSE;
        boolean arg5 = true;

        boolean b = delegate.invoke(arg1, arg2, arg3, arg3, arg5, arg6);
        assertTrue(b);
    }

    public static class InnerClass {
        public static Boolean typedFunction(
            int arg1,
            Integer arg2,
            String arg3,
            Object arg4,
            Boolean arg5,
            boolean arg6
        ) {
            return Boolean.TRUE.booleanValue();
        }
    }
}
