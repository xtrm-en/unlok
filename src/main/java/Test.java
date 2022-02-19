import me.xtrm.unlok.accessor.FieldAccessor;
import org.jetbrains.annotations.Nullable;

public class Test implements FieldAccessor<String> {

    private final POJO instance;

    public Test(POJO object) {
        this.instance = object;
    }

    @Nullable
    @Override
    public String get() {
        return instance.name;
    }

    @Override
    public void set(@Nullable String value) {
        instance.name = value;
    }
}