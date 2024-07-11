package interpreter.parser;

import interpreter.ast.*;
import interpreter.lexer.Lexer;
import interpreter.token.Token;
import interpreter.token.TokenType;
import lombok.Getter;

import java.util.*;

import static interpreter.parser.Precedence.LOWEST;
import static interpreter.parser.Precedence.PRECEDENCES;

@Getter
public class Parser {
    private final Lexer lexer;
    private final List<String> errors;
    private final Map<TokenType, PrefixParseFn<Parser, Expression>> prefixParseFns;
    private final Map<TokenType, InfixParseFn<Parser, Expression, Expression>> infixParseFns;
    private Token currentToken;
    private Token peekToken;


    public Parser(Lexer lexer) {
        this.lexer = lexer;
        errors = new LinkedList<>();
        nextToken();
        nextToken();
        prefixParseFns = new HashMap<>();
        infixParseFns = new HashMap<>();

        registerPrefix(TokenType.IDENT, ParseFns.parseIdentifier);
        registerPrefix(TokenType.INT, ParseFns.parseIntegerLiteral);
        registerPrefix(TokenType.BANG, ParseFns.parsePrefixExpression);
        registerPrefix(TokenType.MINUS, ParseFns.parsePrefixExpression);
        registerPrefix(TokenType.TRUE, ParseFns.parseBoolean);
        registerPrefix(TokenType.FALSE, ParseFns.parseBoolean);
        registerPrefix(TokenType.LPAREN, ParseFns.parseGroupedExpression);
        registerPrefix(TokenType.IF, ParseFns.parseIfExpression);
        registerPrefix(TokenType.FUNCTION, ParseFns.parseFunctionLiteral);
        registerPrefix(TokenType.STRING, ParseFns.parseStringLiteral);
        registerPrefix(TokenType.LBRACKET, ParseFns.parseArrayLiteral);
        registerPrefix(TokenType.LBRACE, ParseFns.parseHashLiteral);

        registerInfix(TokenType.PLUS, ParseFns.parseInfixExpression);
        registerInfix(TokenType.MINUS, ParseFns.parseInfixExpression);
        registerInfix(TokenType.SLASH, ParseFns.parseInfixExpression);
        registerInfix(TokenType.ASTERISK, ParseFns.parseInfixExpression);
        registerInfix(TokenType.EQ, ParseFns.parseInfixExpression);
        registerInfix(TokenType.NOT_EQ, ParseFns.parseInfixExpression);
        registerInfix(TokenType.LT, ParseFns.parseInfixExpression);
        registerInfix(TokenType.LBRACKET, ParseFns.parseIndexExpression);
        registerInfix(TokenType.GT, ParseFns.parseInfixExpression);
        registerInfix(TokenType.LPAREN, ParseFns.parseCallExpression);

    }

    public void nextToken() {
        currentToken = peekToken;
        peekToken = lexer.nextToken();
    }

    public Program parseProgram() {

        List<Statement> statementList = new LinkedList<>();
        while (currentToken.getType() != TokenType.EOF) {
            Optional.ofNullable(parseStatements()).ifPresent(statementList::add);
            nextToken();
        }
        return new Program(statementList.toArray(new Statement[1]));
    }

    protected Expression parseExpression(Precedence precedence) {

        PrefixParseFn<Parser, Expression> prefixFn = prefixParseFns.get(currentToken.getType());

        if (prefixFn == null) {
            noPrefixParseFnError(currentToken.getType());
            return null;
        }

        Expression leftExp = prefixFn.parse(this);

        while (!peekTokenIs(TokenType.SEMICOLON) && precedence.ordinal() < peekPrecedence().ordinal()) {
            InfixParseFn<Parser, Expression, Expression> infixFn = infixParseFns.get(peekToken.getType());

            if (infixFn == null) {
                return leftExp;
            }

            nextToken();
            leftExp = infixFn.parse(this, leftExp);
        }
        return leftExp;
    }

    protected Precedence peekPrecedence() {
        return Optional.ofNullable(PRECEDENCES.get(peekToken.getType()))
                .orElse(LOWEST);
    }

    protected Precedence curPrecedence() {
        return Optional.ofNullable(PRECEDENCES.get(currentToken.getType()))
                .orElse(LOWEST);
    }

    private void registerPrefix(TokenType tokenType, PrefixParseFn<Parser, Expression> fn) {
        prefixParseFns.put(tokenType, fn);
    }

    private void registerInfix(TokenType tokenType, InfixParseFn<Parser, Expression, Expression> fn) {
        infixParseFns.put(tokenType, fn);
    }

    private Statement parseStatements() {
        switch (currentToken.getType()) {
            case LET -> {
                return parseLetStatement();
            }
            case RETURN -> {
                return parseReturnStatement();
            }
            default -> {
                return parseExpressionStatement();
            }
        }
    }

    private ExpressionStatement parseExpressionStatement() {
        Token expressionToken = currentToken;
        Expression exp = parseExpression(Precedence.LOWEST);

        if (peekTokenIs(TokenType.SEMICOLON)) {
            nextToken();
        }

        return new ExpressionStatement(expressionToken, exp);
    }


    private LetStatement parseLetStatement() {
        Token token = currentToken;
        if (!expectPeek(TokenType.IDENT)) {
            return null;
        }

        Identifier identifier = new Identifier(currentToken, currentToken.getLiteral());
        if (!expectPeek(TokenType.ASSIGN)) {
            return null;
        }

        nextToken();
        Expression value = parseExpression(LOWEST);

        if (peekTokenIs(TokenType.SEMICOLON)) {
            nextToken();
        }

        return new LetStatement(token, identifier, value);
    }

    private ReturnStatement parseReturnStatement() {
        Token returnToken = currentToken;

        nextToken();
        Expression returnValue = parseExpression(LOWEST);
        if (peekTokenIs(TokenType.SEMICOLON)) {
            nextToken();
        }

        return new ReturnStatement(returnToken, returnValue);
    }

    protected boolean curTokenIs(TokenType tokenType) {
        return currentToken.getType() == tokenType;
    }

    protected BlockStatement parseBlockStatement() {
        Token blockStatementToken = currentToken;
        List<Statement> statementList = new ArrayList<>();
        nextToken();

        while (!curTokenIs(TokenType.RBRACE) && !curTokenIs(TokenType.EOF)) {
            Statement stmt = parseStatements();
            Optional.ofNullable(stmt).ifPresent(statementList::add);
            nextToken();
        }

        return new BlockStatement(blockStatementToken, statementList.toArray(new Statement[0]));
    }

    protected boolean expectPeek(TokenType tokenType) {
        if (peekTokenIs(tokenType)) {
            nextToken();
            return true;
        } else {
            errors.add(String.format("expected next token to be %s, got %s instead",
                    tokenType.getValue(), peekToken.getType().getValue()));
            return false;
        }
    }

    protected boolean peekTokenIs(TokenType tokenType) {
        return peekToken.getType() == tokenType;
    }

    protected Identifier[] parseFunctionParameters() {
        List<Identifier> identifiers = new ArrayList<>();

        if (peekTokenIs(TokenType.RPAREN)) {
            nextToken();
            return new Identifier[0];
        }

        nextToken();
        Identifier ident = new Identifier(currentToken, currentToken.getLiteral());
        identifiers.add(ident);

        while (peekTokenIs(TokenType.COMMA)) {
            nextToken();
            nextToken();
            ident = new Identifier(currentToken, currentToken.getLiteral());
            identifiers.add(ident);
        }

        if (!expectPeek(TokenType.RPAREN)) {
            return null;
        }

        return identifiers.toArray(new Identifier[0]);
    }

    protected Expression[] parseCallArguments() {
        List<Expression> args = new ArrayList<>();

        if (peekTokenIs(TokenType.RPAREN)) {
            nextToken();
            return new Expression[0];
        }

        nextToken();
        args.add(parseExpression(LOWEST));

        while (peekTokenIs(TokenType.COMMA)) {
            nextToken();
            nextToken();
            args.add(parseExpression(LOWEST));
        }

        if (!expectPeek(TokenType.RPAREN)) {
            return null;
        }

        return args.toArray(new Expression[0]);
    }

    private void noPrefixParseFnError(TokenType type) {
        errors.add(String.format("no prefix parse function for %s found", type.getValue()));
    }

    public Expression[] parseExpressionList(TokenType tokenType) {
        List<Expression> list = new LinkedList<>();

        if (peekTokenIs(tokenType)) {
            nextToken();
            return list.toArray(new Expression[0]);
        }

        nextToken();
        list.add(parseExpression(LOWEST));

        while (peekTokenIs(TokenType.COMMA)) {
            nextToken();
            nextToken();
            list.add(parseExpression(LOWEST));
        }

        if (!expectPeek(tokenType)) {
            return null;
        }

        return list.toArray(new Expression[0]);
    }
}

