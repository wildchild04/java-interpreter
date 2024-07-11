package interpreter.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Program implements Node {

    private Statement[] statements;

    @Override
    public String tokenLiteral() {
        return statements.length > 0 ? statements[0].tokenLiteral() : "";
    }

    @Override
    public NodeType type() {
        return NodeType.PROGRAM;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();

        for (Statement s : statements) {
            out.append(s);
        }

        return out.toString();
    }
}
