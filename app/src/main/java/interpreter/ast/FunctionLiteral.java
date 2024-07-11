package interpreter.ast;

import interpreter.token.Token;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class FunctionLiteral implements Expression {
    private Token token;
    private Identifier[] parameters;
    private BlockStatement body;


    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    @Override
    public NodeType type() {
        return NodeType.FUNCTION_LITERAL;
    }

    @Override
    public String toString() {
        String out = token.getLiteral() +
                "(" +
                Arrays.stream(parameters)
                        .map(Identifier::toString)
                        .collect(Collectors.joining(", ")) +
                ") " +
                body;

        return out;
    }
}
