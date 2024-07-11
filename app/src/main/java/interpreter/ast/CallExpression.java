package interpreter.ast;

import interpreter.token.Token;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class CallExpression implements Expression{
    private Token token;
    private Expression function;
    private Expression[] arguments;

    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    @Override
    public NodeType type() {
        return NodeType.CALL_EXPRESSION;
    }

    @Override
    public String toString() {
        String out = function.toString()
                + "("
                + Arrays.stream(arguments)
                .map(Objects::toString)
                .collect(Collectors.joining(", "))
                + ")";

        return out;
    }
}
