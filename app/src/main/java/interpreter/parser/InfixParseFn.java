package interpreter.parser;

import interpreter.ast.Expression;

@FunctionalInterface
public interface InfixParseFn<P extends Parser, L extends Expression, E extends Expression> {
    E parse(P p, L l);
}
