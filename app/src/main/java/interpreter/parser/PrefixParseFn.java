package interpreter.parser;

import interpreter.ast.Expression;

@FunctionalInterface
public interface PrefixParseFn<P extends Parser, E extends Expression> {
    E parse(P p);
}
