package interpreter.monkey.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class MonkeyArray implements MonkeyObject{
    public static String ARRAY_OBJ = "ARRAY";
    private MonkeyObject[] elements;

    @Override
    public String type() {
        return ARRAY_OBJ;
    }

    @Override
    public String inspect() {
        String out ="";

        out += "[";
        out += Arrays.stream(elements).map(MonkeyObject::inspect).collect(Collectors.joining(", "));
        out += "]";

        return out;
    }
}
