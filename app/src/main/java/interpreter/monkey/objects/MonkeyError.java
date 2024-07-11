package interpreter.monkey.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MonkeyError implements MonkeyObject {

    public static String ERROR_OBJ = "ERROR";
    private String message;

    @Override
    public String type() {
        return ERROR_OBJ;
    }

    @Override
    public String inspect() {
        return "ERROR: " + message;
    }
}
