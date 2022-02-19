import me.xtrm.unlok.accessor.FieldAccessor;
import org.jetbrains.annotations.Nullable;

public class POJO implements FieldAccessor<String> {
    public String name = "";

    public static String NAME = "NamePOJOStatic";

    @Nullable
    @Override
    public String get() {
        return null;
    }

    @Override
    public void set(@Nullable String value) {

    }
}
