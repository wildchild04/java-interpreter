package interpreter.ast;

import interpreter.token.Token;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class InfixExpression implements  Expression {
    private Token token;
    private Expression left;
    private Expression right;
    private String operator;

    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    @Override
    public NodeType type() {
        return NodeType.INFIX_EXPRESSION;
    }

    @Override
    public String toString() {
        return "("
                + left
                + " "
                + operator
                + " "
                + right
                + ")";
    }

}
