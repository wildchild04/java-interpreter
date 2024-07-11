package interpreter.ast;

import interpreter.token.Token;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ReturnStatement implements Statement{

    private Token token;
    private Expression returnValue;

    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    @Override
    public NodeType type() {
        return NodeType.RETURN_STATEMENT;
    }
}
