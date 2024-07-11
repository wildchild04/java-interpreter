package interpreter.ast;

import interpreter.token.Token;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class IndexExpression implements Expression{

    private Token token;
    private Expression left;
    private Expression index;

    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    @Override
    public NodeType type() {
        return NodeType.INDEX_EXPRESSION;
    }

    @Override
    public String toString() {
        String out = "";

        out += "(";
        out += left.toString();
        out += "[";
        out += index.toString();
        out += "])";

        return out;
    }
}
