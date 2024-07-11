package interpreter.ast;

import interpreter.token.Token;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BlockStatement implements Expression {
    private Token token;
    private Statement[] statements;

    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    @Override
    public NodeType type() {
        return NodeType.BLOCK_STATEMENT;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();

        for(Statement s: statements) {
            out.append(s);
        }

        return out.toString();
    }
}
