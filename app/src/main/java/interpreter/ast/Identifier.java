package interpreter.ast;

import interpreter.token.Token;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Identifier implements Statement, Expression{

    private Token token;
    private String value;

    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    @Override
    public NodeType type() {
        return NodeType.IDENTIFIER;
    }

    @Override
    public String toString() {
        return value;
    }
}
