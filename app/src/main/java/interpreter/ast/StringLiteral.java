package interpreter.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static interpreter.ast.NodeType.STRING;

@AllArgsConstructor
@Getter
public class StringLiteral implements Expression{
    private String value;

    @Override
    public String tokenLiteral() {
        return value;
    }

    @Override
    public NodeType type() {
        return STRING;
    }
}
