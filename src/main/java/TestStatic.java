import me.xtrm.unlok.accessor.FieldAccessor;
import org.jetbrains.annotations.Nullable;

public class TestStatic implements FieldAccessor<String> {
    @Nullable
    @Override
    public String get() {
        return POJO.NAME;
    }

    @Override
    public void set(@Nullable String value) {
        POJO.NAME = value;
    }
}
