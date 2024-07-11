package interpreter.monkey.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.charset.StandardCharsets;

@AllArgsConstructor
@Getter
public class MonkeyString implements MonkeyObject, Hashable {

    public static String STRING_OBJ = "STRING";

    private String value;

    @Override
    public String type() {
        return STRING_OBJ;
    }

    @Override
    public String inspect() {
        return value;
    }

    public MonkeyHashKey hashKey() {
        long hash = FNVHash.fnv1a64(value.getBytes(StandardCharsets.UTF_8));
        return new MonkeyHashKey(type(), hash);
    }
}
