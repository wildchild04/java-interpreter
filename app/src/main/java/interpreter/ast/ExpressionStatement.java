package interpreter.ast;

import interpreter.monkey.objects.MonkeyObject;
import interpreter.token.Token;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

@AllArgsConstructor
@Getter
public class ExpressionStatement implements Statement{
    private Token token;
    private Expression expression;

    @Override
    public String tokenLiteral() {
        return token.getLiteral();
    }

    @Override
    public NodeType type() {
        return NodeType.EXPRESSION_STATEMENT;
    }

    @Override
    public String toString() {
        return Optional.ofNullable(expression).map(Object::toString).orElse("");
    }

}
