package interpreter.token;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
import java.util.Optional;

@Data
@AllArgsConstructor
public class Token {
    private static final Map<String, TokenType> keywords = Map.of(
            "fn", TokenType.FUNCTION,
            "let", TokenType.LET,
            "true", TokenType.TRUE,
            "false", TokenType.FALSE,
            "if", TokenType.IF,
            "else", TokenType.ELSE,
            "return", TokenType.RETURN
    );

    private TokenType type;
    private String literal;

    public static TokenType lookupIdent(String ident) {
        return Optional.ofNullable(keywords.get(ident)).orElse(TokenType.IDENT);
    }

}