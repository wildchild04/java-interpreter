package interpreter.ast;

import interpreter.token.Token;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class IfExpression implements Expression {
    private Token token;
    private Expression condition;
    private BlockStatement consequence;
    private BlockStatement alternative;

    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    @Override
    public NodeType type() {
        return NodeType.IF_EXPRESSION;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();

        out.append("if");
        out.append(condition);
        out.append(" ");
        out.append(consequence);

        if (alternative != null) {
            out.append("else ");
            out.append(alternative);
        }

        return out.toString();
    }
}
