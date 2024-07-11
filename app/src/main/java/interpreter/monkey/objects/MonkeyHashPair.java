package interpreter.monkey.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter

public class MonkeyHashPair {
    private MonkeyObject key;
    private MonkeyObject value;

    public String toString() {
        return String.format("key %s, value %s", key.inspect(), value.inspect());
    }
}
