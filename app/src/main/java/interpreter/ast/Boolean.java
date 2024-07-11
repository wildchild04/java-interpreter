package interpreter.ast;

import interpreter.token.Token;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static interpreter.ast.NodeType.BOOLEAN;

@AllArgsConstructor
@Getter
public class Boolean implements Expression{
    private Token token;
    private boolean value;

    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    @Override
    public NodeType type() {
        return BOOLEAN;
    }

    @Override
    public String toString(){
        return token.getLiteral();
    }
}
