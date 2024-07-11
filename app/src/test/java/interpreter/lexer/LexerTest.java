package interpreter.lexer;

import interpreter.token.Token;
import interpreter.token.TokenType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LexerTest {

    @Test
    void testNextToken() {

        String input = """
                let five = 5;
                let ten = 10;
                let add = fn(x,y) {
                x+y;
                };
                let result = add(five,ten);
                !-/*5;
                5 < 10 > 5;
                if (5 < 10) { return true; } else { return false; }
                10 == 10;
                10 != 9;
                "foobar"
                "foo bar"
                [1,2];
                {"foo":"bar"}
                """;
        List<Arguments> expectedTokens = List.of(
                Arguments.of(TokenType.LET, "let"),
                Arguments.of(TokenType.IDENT, "five"),
                Arguments.of(TokenType.ASSIGN, "="),
                Arguments.of(TokenType.INT, "5"),
                Arguments.of(TokenType.SEMICOLON, ";"),
                Arguments.of(TokenType.LET, "let"),
                Arguments.of(TokenType.IDENT, "ten"),
                Arguments.of(TokenType.ASSIGN, "="),
                Arguments.of(TokenType.INT, "10"),
                Arguments.of(TokenType.SEMICOLON, ";"),
                Arguments.of(TokenType.LET, "let"),
                Arguments.of(TokenType.IDENT, "add"),
                Arguments.of(TokenType.ASSIGN, "="),
                Arguments.of(TokenType.FUNCTION, "fn"),
                Arguments.of(TokenType.LPAREN, "("),
                Arguments.of(TokenType.IDENT, "x"),
                Arguments.of(TokenType.COMMA, ","),
                Arguments.of(TokenType.IDENT, "y"),
                Arguments.of(TokenType.RPAREN, ")"),
                Arguments.of(TokenType.LBRACE, "{"),
                Arguments.of(TokenType.IDENT, "x"),
                Arguments.of(TokenType.PLUS, "+"),
                Arguments.of(TokenType.IDENT, "y"),
                Arguments.of(TokenType.SEMICOLON, ";"),
                Arguments.of(TokenType.RBRACE, "}"),
                Arguments.of(TokenType.SEMICOLON, ";"),
                Arguments.of(TokenType.LET, "let"),
                Arguments.of(TokenType.IDENT, "result"),
                Arguments.of(TokenType.ASSIGN, "="),
                Arguments.of(TokenType.IDENT, "add"),
                Arguments.of(TokenType.LPAREN, "("),
                Arguments.of(TokenType.IDENT, "five"),
                Arguments.of(TokenType.COMMA, ","),
                Arguments.of(TokenType.IDENT, "ten"),
                Arguments.of(TokenType.RPAREN, ")"),
                Arguments.of(TokenType.SEMICOLON, ";"),
                Arguments.of(TokenType.BANG, "!"),
                Arguments.of(TokenType.MINUS, "-"),
                Arguments.of(TokenType.SLASH, "/"),
                Arguments.of(TokenType.ASTERISK, "*"),
                Arguments.of(TokenType.INT, "5"),
                Arguments.of(TokenType.SEMICOLON, ";"),
                Arguments.of(TokenType.INT, "5"),
                Arguments.of(TokenType.LT, "<"),
                Arguments.of(TokenType.INT, "10"),
                Arguments.of(TokenType.GT, ">"),
                Arguments.of(TokenType.INT, "5"),
                Arguments.of(TokenType.SEMICOLON, ";"),
                Arguments.of(TokenType.IF, "if"),
                Arguments.of(TokenType.LPAREN, "("),
                Arguments.of(TokenType.INT, "5"),
                Arguments.of(TokenType.LT, "<"),
                Arguments.of(TokenType.INT, "10"),
                Arguments.of(TokenType.RPAREN, ")"),
                Arguments.of(TokenType.LBRACE, "{"),
                Arguments.of(TokenType.RETURN, "return"),
                Arguments.of(TokenType.TRUE, "true"),
                Arguments.of(TokenType.SEMICOLON, ";"),
                Arguments.of(TokenType.RBRACE, "}"),
                Arguments.of(TokenType.ELSE, "else"),
                Arguments.of(TokenType.LBRACE, "{"),
                Arguments.of(TokenType.RETURN, "return"),
                Arguments.of(TokenType.FALSE, "false"),
                Arguments.of(TokenType.SEMICOLON, ";"),
                Arguments.of(TokenType.RBRACE, "}"),
                Arguments.of(TokenType.INT, "10"),
                Arguments.of(TokenType.EQ, "=="),
                Arguments.of(TokenType.INT, "10"),
                Arguments.of(TokenType.SEMICOLON, ";"),
                //10 != 9;
                Arguments.of(TokenType.INT, "10"),
                Arguments.of(TokenType.NOT_EQ, "!="),
                Arguments.of(TokenType.INT, "9"),
                Arguments.of(TokenType.SEMICOLON, ";"),
                Arguments.of(TokenType.STRING, "foobar"),
                Arguments.of(TokenType.STRING, "foo bar"),
                Arguments.of(TokenType.LBRACKET,"["),
                Arguments.of(TokenType.INT, "1"),
                Arguments.of(TokenType.COMMA, ","),
                Arguments.of(TokenType.INT, "2"),
                Arguments.of(TokenType.RBRACKET,"]"),
                Arguments.of(TokenType.SEMICOLON, ";"),
                Arguments.of(TokenType.LBRACE, "{"),
                Arguments.of(TokenType.STRING, "foo"),
                Arguments.of(TokenType.COLON, ":"),
                Arguments.of(TokenType.STRING, "bar"),
                Arguments.of(TokenType.RBRACE, "}"),
                Arguments.of(TokenType.EOF, "")
        );


        Lexer lexer = new Lexer(input);

        for (Arguments expected : expectedTokens) {
            Token currentToken = lexer.nextToken();

            assertEquals(expected.get()[0], currentToken.getType(), "wrong token type");
            assertEquals(expected.get()[1], currentToken.getLiteral(), "wrong token literal");

        }
    }

}
