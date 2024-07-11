package interpreter.token;

public enum TokenType {

    ILLEGAL("ILLEGAL"),
    EOF("EOF"),
    IDENT("IDENT"),
    INT("INT"),
    ASSIGN("="),
    PLUS("+"),
    MINUS("-"),
    BANG("!"),
    ASTERISK("*"),
    SLASH("/"),
    COMMA(","),
    SEMICOLON(":"),
    LT("<"),
    GT(">"),
    LPAREN("("),
    RPAREN(")"),
    LBRACE("{"),
    RBRACE("}"),
    FUNCTION("fn"),
    LET("LET"),
    TRUE("TRUE"),
    FALSE("FALSE"),
    IF("IF"),
    ELSE("ELSE"),
    RETURN("RETURN"),
    EQ("=="),
    NOT_EQ("!="),
    STRING("STRING"),
    LBRACKET("["),
    RBRACKET("]"),
    COLON(":");

    private final String value;

    TokenType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
