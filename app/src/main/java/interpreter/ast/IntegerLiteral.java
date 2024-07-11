package interpreter.ast;

import interpreter.token.Token;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class IntegerLiteral implements Expression {

    private Token token;
    private int value;

    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    @Override
    public NodeType type() {
        return NodeType.INTEGER;
    }

    @Override
    public String toString() {
        return token.getLiteral();
    }
}
