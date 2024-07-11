package interpreter.monkey.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MonkeyInteger implements MonkeyObject, Hashable {
    public static String MONKEY_INT = "INTEGER";
    private int value;

    @Override
    public String type() {
        return MONKEY_INT;
    }

    @Override
    public String inspect() {
        return Integer.toString(value);
    }

    public MonkeyHashKey hashKey() {
        return new MonkeyHashKey(type(), value);
    }
}
