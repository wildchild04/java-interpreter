package interpreter.ast;

import interpreter.token.Token;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

@AllArgsConstructor
@Getter
public class LetStatement implements Statement {

    private Token token;
    private Identifier name;
    private Expression value;

    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    @Override
    public NodeType type() {
        return NodeType.LET_STATEMENT;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();

        out.append(tokenLiteral());
        out.append(" ");
        out.append(name);
        out.append(" = ");
        Optional.ofNullable(value)
                .ifPresent(out::append);
        out.append(";");

        return out.toString();
    }
}
