package interpreter.lexer;

import interpreter.token.Token;
import interpreter.token.TokenType;

public class Lexer {

    private final String input;
    private int position;
    private int readPosition;
    private char ch;

    public Lexer(String input) {
        this.input = input;
        readChar();
    }

    public Token nextToken() {
        Token token;
        skipWitheSpace();
        switch (ch) {
            case '=' -> {
                if (peekChar() == '=') {
                    char current = ch;
                    readChar();
                    String literal = String.join("",Character.toString(current), Character.toString(ch));
                    token = new Token(TokenType.EQ, literal);
                } else {
                    token = new Token(TokenType.ASSIGN, Character.toString(ch));
                }
            }
            case '+' -> token = new Token(TokenType.PLUS, Character.toString(ch));
            case '-' -> token = new Token(TokenType.MINUS, Character.toString(ch));
            case '!' -> {
                if (peekChar() == '=') {
                    char current = ch;
                    readChar();
                    String literal = String.join("",Character.toString(current), Character.toString(ch));
                    token = new Token(TokenType.NOT_EQ, literal);
                } else {
                    token = new Token(TokenType.BANG, Character.toString(ch));
                }
            }
            case '/' -> token = new Token(TokenType.SLASH, Character.toString(ch));
            case '*' -> token = new Token(TokenType.ASTERISK, Character.toString(ch));
            case '<' -> token = new Token(TokenType.LT, Character.toString(ch));
            case '>' -> token = new Token(TokenType.GT, Character.toString(ch));
            case ';' -> token = new Token(TokenType.SEMICOLON, Character.toString(ch));
            case ',' -> token = new Token(TokenType.COMMA, Character.toString(ch));
            case '(' -> token = new Token(TokenType.LPAREN, Character.toString(ch));
            case ')' -> token = new Token(TokenType.RPAREN, Character.toString(ch));
            case '{' -> token = new Token(TokenType.LBRACE, Character.toString(ch));
            case '}' -> token = new Token(TokenType.RBRACE, Character.toString(ch));
            case '"' -> token = new Token(TokenType.STRING, readString());
            case '[' -> token = new Token(TokenType.LBRACKET, Character.toString(ch));
            case ']' -> token = new Token(TokenType.RBRACKET, Character.toString(ch));
            case ':' -> token = new Token(TokenType.COLON, Character.toString(ch));
            case 0 -> token = new Token(TokenType.EOF, "");
            default -> {
                if (isLetter(ch)) {
                    String literal = readIdentifier();
                    return new Token(Token.lookupIdent(literal), literal);
                } else if (Character.isDigit(ch)) {
                    return new Token(TokenType.INT, readNumber());
                } else {
                    token = new Token(TokenType.ILLEGAL, Character.toString(ch));
                }
            }
        }
        readChar();
        return token;
    }

    private String readString() {
        int start = position;
        do {
            readChar();
        } while (ch != '"' && ch != 0);

        return input.substring(start+1, position);
    }

    private String readNumber() {
        int startPosition = position;
        while (Character.isDigit(ch)) {
            readChar();
        }
        return input.substring(startPosition, position);
    }

    private String readIdentifier() {
        int startPosition = position;
        while (isLetter(ch)) {
            readChar();
        }
        return input.substring(startPosition, position);
    }

    private Boolean isLetter(char ch) {
        return Character.isLetter(ch) || ch == '_';
    }

    private void readChar() {
        if (readPosition >= input.length()) {
            ch = 0;
        } else {
            ch = input.charAt(readPosition);
        }
        position = readPosition;
        readPosition++;
    }

    private void skipWitheSpace() {
        while (Character.isWhitespace(ch)) {
            readChar();
        }
    }

    private char peekChar() {
        if (readPosition >= input.length()) {
            return 0;
        } else {
            return input.charAt(readPosition);
        }
    }
}
