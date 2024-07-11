package interpreter.monkey.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Function;

@AllArgsConstructor
@Getter
public class MonkeyBuiltin implements MonkeyObject{
    public static String BUILTIN_OBJ = "BUILTIN";

    private Function<MonkeyObject[], MonkeyObject> builtinFunction;

    @Override
    public String type() {
        return BUILTIN_OBJ;
    }

    @Override
    public String inspect() {
        return "builtin function";
    }
}
