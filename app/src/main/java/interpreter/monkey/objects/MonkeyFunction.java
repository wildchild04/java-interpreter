package interpreter.monkey.objects;

import interpreter.ast.BlockStatement;
import interpreter.ast.Identifier;
import interpreter.environment.Environment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class MonkeyFunction implements MonkeyObject{
    public static String FUNCTION_OBJ = "FUNCTION";

    private Identifier[] parameters;
    private BlockStatement body;
    private Environment env;

    @Override
    public String type() {
        return FUNCTION_OBJ;
    }

    @Override
    public String inspect() {
        String out = "";
        out += "fn";
        out += "(";
        out += Arrays.stream(parameters)
                .map(Identifier::toString)
                .collect(Collectors.joining());
        out += ") {\n";
        out += body.toString();
        out += "\n}";
        return out;
    }
}
