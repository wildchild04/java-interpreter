package interpreter.monkey.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MonkeyBoolean implements MonkeyObject, Hashable{
    public static String MONKEY_BOOLEAN = "BOOLEAN";
    private boolean value;

    @Override
    public String type() {
        return MONKEY_BOOLEAN;
    }

    @Override
    public String inspect() {
        return Boolean.toString(value);
    }

    public MonkeyHashKey hashKey() {
        long val = value ? 1 : 0;
        return new MonkeyHashKey(type(), val);
    }
}
