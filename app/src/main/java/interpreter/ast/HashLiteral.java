package interpreter.ast;

import interpreter.token.Token;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static interpreter.ast.NodeType.HASH;

@AllArgsConstructor
@Getter
public class HashLiteral implements Expression{

    private Token token;
    private Map<Expression, Expression> pairs;

    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    @Override
    public NodeType type() {
        return HASH;
    }

    @Override
    public String toString() {
        String out = "";

        out +="{";
        out += pairs.entrySet()
                .stream()
                .map(e -> String.format("%s:%s", e.getKey(), e.getValue()))
                .collect(Collectors.joining(", "));
        out += "}";

        return out;
    }
}
