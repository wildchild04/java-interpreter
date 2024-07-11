package interpreter.monkey.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class MonkeyHash implements MonkeyObject {
    public static String HASH_OBJ ="HASH";

    private Map<MonkeyHashKey,MonkeyHashPair> pairs;

    @Override
    public String type() {
        return HASH_OBJ;
    }

    @Override
    public String inspect() {
        String out ="";

        out += "{";
        out += pairs.values()
                .stream()
                .map(v -> String.format("%s: %s", v.getKey().inspect(), v.getValue().inspect()))
                .collect(Collectors.joining(", "));
        out += "}";

        return out;
    }
}
