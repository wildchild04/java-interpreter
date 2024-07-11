package interpreter.parser;

import interpreter.token.TokenType;

import java.util.Map;

public enum Precedence {
    LOWEST,
    EQUALS,
    LESSGREATER,
    SUM,
    PRODUCT,
    PREFIX,
    CALL,
    INDEX;

    public static final Map<TokenType, Precedence> PRECEDENCES = Map.of(
            TokenType.EQ, EQUALS,
            TokenType.NOT_EQ, EQUALS,
            TokenType.LT, LESSGREATER,
            TokenType.GT, LESSGREATER,
            TokenType.PLUS, SUM,
            TokenType.MINUS, SUM,
            TokenType.SLASH, PRODUCT,
            TokenType.ASTERISK, PRODUCT,
            TokenType.LPAREN, CALL,
            TokenType.LBRACKET, INDEX
    );

    public static Precedence getFromOrdinal(int ordinal) {
        var values = Precedence.values();
        if (ordinal>values.length) {
            return LOWEST;
        }

        return values[ordinal];
    }



}
