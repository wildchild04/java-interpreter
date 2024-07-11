package interpreter.parser;

import interpreter.ast.Boolean;
import interpreter.ast.*;
import interpreter.token.Token;
import interpreter.token.TokenType;

import java.util.HashMap;
import java.util.Map;

public class ParseFns {
    public static final PrefixParseFn<Parser, Expression> parseIdentifier =
            parser -> new Identifier(parser.getCurrentToken(), parser.getCurrentToken().getLiteral());

    public static final PrefixParseFn<Parser, Expression> parseIntegerLiteral =
            parser -> {
                Token integerToken = parser.getCurrentToken();
                try {
                    int value = Integer.parseInt(integerToken.getLiteral());
                    return new IntegerLiteral(integerToken, value);
                } catch (NumberFormatException ex) {
                    parser.getErrors().add(ex.getMessage());
                    return null;
                }
            };

    public static PrefixParseFn<Parser, Expression> parsePrefixExpression =
            parser -> {
                Token expressionToken = parser.getCurrentToken();
                parser.nextToken();
                Expression right = parser.parseExpression(Precedence.PREFIX);
                return new PrefixExpression(expressionToken, expressionToken.getLiteral(), right);
            };

    public static PrefixParseFn<Parser, Expression> parseBoolean =
            parser -> new Boolean(parser.getCurrentToken(), parser.curTokenIs(TokenType.TRUE));

    public static PrefixParseFn<Parser, Expression> parseGroupedExpression =
            parser -> {
                parser.nextToken();
                Expression exp = parser.parseExpression(Precedence.LOWEST);

                if (!parser.expectPeek(TokenType.RPAREN)) {
                    return null;
                }

                return exp;
            };

    public static PrefixParseFn<Parser, Expression> parseIfExpression =
            parser -> {
                Token ifExpressionToken = parser.getCurrentToken();

                if (!parser.expectPeek(TokenType.LPAREN)) {
                    return null;
                }

                parser.nextToken();
                Expression condition = parser.parseExpression(Precedence.LOWEST);

                if (!parser.expectPeek(TokenType.RPAREN)) {
                    return null;
                }

                if (!parser.expectPeek(TokenType.LBRACE)) {
                    return null;
                }

                BlockStatement consequence = parser.parseBlockStatement();
                BlockStatement alternative = null;

                if (parser.peekTokenIs(TokenType.ELSE)) {
                    parser.nextToken();

                    if (!parser.peekTokenIs(TokenType.LBRACE)) {
                        return null;
                    }
                    parser.nextToken();
                    alternative = parser.parseBlockStatement();
                }

                return new IfExpression(ifExpressionToken, condition, consequence, alternative);
            };

    public static PrefixParseFn<Parser, Expression> parseFunctionLiteral =
            parser -> {
                Token functionToken = parser.getCurrentToken();

                if (!parser.expectPeek(TokenType.LPAREN)) {
                    return null;
                }
                Identifier[] parameters = parser.parseFunctionParameters();

                if (!parser.expectPeek(TokenType.LBRACE)) {
                    return null;
                }

                BlockStatement body = parser.parseBlockStatement();

                return new FunctionLiteral(functionToken, parameters, body);
            };

    public static InfixParseFn<Parser, Expression, Expression> parseInfixExpression =
            (parser, left) -> {
                Token expressionToken = parser.getCurrentToken();
                Precedence precedence = parser.curPrecedence();
                parser.nextToken();

                Expression right = parser.parseExpression(precedence);

                return new InfixExpression(expressionToken, left, right, expressionToken.getLiteral());
            };

    public static InfixParseFn<Parser, Expression, Expression> parseCallExpression =
            (parser, function) -> {
                Token callExpToken = parser.getCurrentToken();
                Expression[] args = parser.parseExpressionList(TokenType.RPAREN);
                return new CallExpression(callExpToken, function, args);
            };

    public static PrefixParseFn<Parser, Expression> parseStringLiteral =
            parser -> new StringLiteral(parser.getCurrentToken().getLiteral());

    public static PrefixParseFn<Parser, Expression> parseArrayLiteral =
            parser -> {
                Token arrayToken = parser.getCurrentToken();
                Expression[] elements = parser.parseExpressionList(TokenType.RBRACKET);

                return new ArrayLiteral(arrayToken, elements);
            };

    public static InfixParseFn<Parser, Expression, Expression> parseIndexExpression = (parser, left) -> {
        Token indexToken = parser.getCurrentToken();
        parser.nextToken();
        Expression index = parser.parseExpression(Precedence.LOWEST);

        if (!parser.expectPeek(TokenType.RBRACKET)){
            return null;
        }

        return new IndexExpression(indexToken, left, index);
    };

    public static PrefixParseFn<Parser, Expression> parseHashLiteral = parser -> {
        Token hashToken = parser.getCurrentToken();
        Map<Expression,Expression> hashMap = new HashMap<>();

        while(!parser.peekTokenIs(TokenType.RBRACE)) {
            parser.nextToken();
            Expression key = parser.parseExpression(Precedence.LOWEST);

            if (!parser.expectPeek(TokenType.COLON)) {
                return null;
            }

            parser.nextToken();
            Expression value = parser.parseExpression(Precedence.LOWEST);

            hashMap.put(key, value);

            if (!parser.peekTokenIs(TokenType.RBRACE) && !parser.expectPeek(TokenType.COMMA)) {
                return null;
            }
        }

        if(!parser.expectPeek(TokenType.RBRACE)) {
            return null;
        }

        return new HashLiteral(hashToken,hashMap);
    };
}
