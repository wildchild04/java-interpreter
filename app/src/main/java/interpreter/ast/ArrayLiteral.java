package interpreter.ast;

import interpreter.token.Token;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class ArrayLiteral implements Expression{

    private Token token;
    private Expression[] elements;

    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    @Override
    public String toString() {
        String out = "";

        out += "[";
        out += Arrays.stream(elements).map(Objects::toString).collect(Collectors.joining(", "));
        out += "]";

        return out;
    }

    @Override
    public NodeType type() {
        return NodeType.ARRAY_LIST;
    }
}
