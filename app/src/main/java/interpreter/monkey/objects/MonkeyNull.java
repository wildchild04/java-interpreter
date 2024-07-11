package interpreter.monkey.objects;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MonkeyNull implements MonkeyObject{
    public static String MONKEY_NULL = "NULL";

    @Override
    public String type() {
        return MONKEY_NULL;
    }

    @Override
    public String inspect() {
        return "null";
    }
}
