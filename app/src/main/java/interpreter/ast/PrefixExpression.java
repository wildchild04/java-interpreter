package interpreter.ast;

import interpreter.token.Token;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PrefixExpression implements Expression {
    private Token token;
    private String operator;
    private Expression right;

    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    @Override
    public NodeType type() {
        return NodeType.PREFIX_EXPRESSION;
    }

    @Override
    public String toString() {

        return "(" +
                operator +
                right +
                ")";
    }
}
