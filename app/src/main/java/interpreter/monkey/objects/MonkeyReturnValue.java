package interpreter.monkey.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MonkeyReturnValue implements MonkeyObject{
    public static String RETURN_VALUE_OBJ = "RETURN_VALUE";

    private MonkeyObject value;

    @Override
    public String type() {
        return RETURN_VALUE_OBJ;
    }

    @Override
    public String inspect() {
        return value.inspect();
    }
}
