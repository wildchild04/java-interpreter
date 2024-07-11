package interpreter.parser;

import interpreter.ast.*;
import interpreter.lexer.Lexer;
import interpreter.token.Token;
import interpreter.token.TokenType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.Boolean;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;


public class ParserTest {

    @ParameterizedTest
    @CsvSource({
            "let x=5;,x,int,5",
            "let y=true;,y,bool,true",
            "let foobar = y;,foobar,string,y"
    })
    void testLetStatements(String input, String expectedIdentifier, String type, String val) {

        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();

        assertNotNull(program);

        assertEquals(0, parser.getErrors().size(), String.format("Errors found %s", parser.getErrors()));
        assertEquals(program.getStatements().length, 1);
        testLetStatement(program.getStatements()[0], expectedIdentifier);
        assertInstanceOf(LetStatement.class, program.getStatements()[0]);
        LetStatement stmt = (LetStatement) program.getStatements()[0];
        testLetStatement(stmt, expectedIdentifier);
        Object value = null;
        switch (type) {
            case "int" -> {
                value = Integer.parseInt(val);
            }
            case "bool" -> {
                value = Boolean.valueOf(val);
            }
            case "string" -> {
                value = val;
            }
        }
        testLiteralExpression(stmt.getValue(), value);
    }

    @ParameterizedTest
    @CsvSource({
            "return 5;,5",
            "return 10;,10",
            "return 993322;,993322",
    })
    void testReturnStatement(String input, String expected) {

        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();

        assertEquals(0, parser.getErrors().size(), String.format("Errors found %s", parser.getErrors()));
        assertEquals(1, program.getStatements().length);
        Statement stmt = program.getStatements()[0];
        assertInstanceOf(ReturnStatement.class, stmt);
        ReturnStatement returnStmt = (ReturnStatement) stmt;
        assertEquals("return", returnStmt.tokenLiteral());
        assertEquals(expected, returnStmt.getReturnValue().tokenLiteral());
    }

    @Test
    void testString() {
        Program program = new Program(new Statement[]{
                new LetStatement(
                        new Token(TokenType.LET, "let"),
                        new Identifier(new Token(TokenType.IDENT, "myVar"), "myVar"),
                        new Identifier(new Token(TokenType.IDENT, "anotherVar"), "anotherVar")
                ),

        });
        assertEquals("let myVar = anotherVar;", program.toString());
    }

    @Test
    void testIdentifierExpression() {
        String input = "foobar";

        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();

        assertEquals(0, parser.getErrors().size(), String.format("Errors found %s", parser.getErrors()));
        assertEquals(1, program.getStatements().length);
        assertInstanceOf(ExpressionStatement.class, program.getStatements()[0]);
        ExpressionStatement stmt = (ExpressionStatement) program.getStatements()[0];
        assertInstanceOf(Identifier.class, stmt.getExpression());
        Identifier indent = (Identifier) stmt.getExpression();
        assertEquals("foobar", indent.getValue());
        assertEquals("foobar", indent.tokenLiteral());

    }

    @Test
    void testIntegerLiteralExpression() {
        String input = "5;";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();

        assertEquals(0, parser.getErrors().size(), String.format("Errors found %s", parser.getErrors()));
        assertEquals(1, program.getStatements().length);
        assertInstanceOf(ExpressionStatement.class, program.getStatements()[0]);
        ExpressionStatement stmt = (ExpressionStatement) program.getStatements()[0];
        assertInstanceOf(IntegerLiteral.class, stmt.getExpression());
        IntegerLiteral literal = (IntegerLiteral) stmt.getExpression();
        assertEquals(5, literal.getValue());
        assertEquals("5", literal.tokenLiteral());
    }

    @ParameterizedTest
    @CsvSource({
            "!5;,!,5",
            "-15;,-,15",
    })
    void testParsingPrefixExpression(String input, String operator, int integerValue) {

        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();

        assertEquals(0, parser.getErrors().size(), String.format("Errors found %s", parser.getErrors()));
        assertEquals(1, program.getStatements().length);
        assertInstanceOf(ExpressionStatement.class, program.getStatements()[0]);
        ExpressionStatement stmt = (ExpressionStatement) program.getStatements()[0];
        assertInstanceOf(PrefixExpression.class, stmt.getExpression());
        PrefixExpression exp = (PrefixExpression) stmt.getExpression();
        assertEquals(operator, exp.getOperator());

        testIntegerLiteral(exp.getRight(), integerValue);
    }

    @ParameterizedTest
    @CsvSource({
            "!true,!,true",
            "!false,!,false"
    })
    void testParsingPrefixExpression(String input, String operator, boolean integerValue) {

        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();

        assertEquals(0, parser.getErrors().size(), String.format("Errors found %s", parser.getErrors()));
        assertEquals(1, program.getStatements().length);
        assertInstanceOf(ExpressionStatement.class, program.getStatements()[0]);
        ExpressionStatement stmt = (ExpressionStatement) program.getStatements()[0];
        assertInstanceOf(PrefixExpression.class, stmt.getExpression());
        PrefixExpression exp = (PrefixExpression) stmt.getExpression();
        assertEquals(operator, exp.getOperator());

        testBooleanLiteral(exp.getRight(), integerValue);
    }

    @ParameterizedTest
    @CsvSource({
            "-a * b,((-a) * b)",
            "!-a,(!(-a))",
            "a + b + c,((a + b) + c)",
            "a + b - c,((a + b) - c)",
            "a * b * c,((a * b) * c)",
            "a * b / c,((a * b) / c)",
            "a + b / c,(a + (b / c))",
            "a + b * c + d / e - f,(((a + (b * c)) + (d / e)) - f)",
            "3 + 4; -5 * 5,(3 + 4)((-5) * 5)",
            "5 > 4 == 3 < 4,((5 > 4) == (3 < 4))",
            "5 < 4 != 3 > 4,((5 < 4) != (3 > 4))",
            "3 + 4 * 5 == 3 * 1 + 4 * 5,((3 + (4 * 5)) == ((3 * 1) + (4 * 5)))",
            "true,true",
            "false,false",
            "3 > 5 == false, ((3 > 5) == false)",
            "3 < 5 == true, ((3 < 5) == true)",
            "1 + (2 + 3) + 4, ((1 + (2 + 3)) + 4)",
            "(5 + 5) * 2, ((5 + 5) * 2)",
            "2 / (5 + 5), (2 / (5 + 5))",
            "-(5 + 5), (-(5 + 5))",
            "!(true == true), (!(true == true))",
            "a + add(b * c) + d,((a + add((b * c))) + d)",
            "add(a@ b@ 1@ 2 * 3@ 4 + 5@ add(6@ 7 * 8)),add(a@ b@ 1@ (2 * 3)@ (4 + 5)@ add(6@ (7 * 8)))",
            "add(a + b + c * d / f + g),add((((a + b) + ((c * d) / f)) + g))",
            "a * [1@ 2@ 3@ 4][ b * c] * d,((a * ([1@ 2@ 3@ 4][(b * c)])) * d)",
            "add( a * b[ 2]@ b[ 1]@ 2 * [1@ 2][ 1]), add((a * (b[2]))@ (b[1])@ (2 * ([1@ 2][1])))",
    })
    void testOperatorPrecedenceParsing(String input, String expected) {
        input = input.replace('@', ',');
        expected = expected.replace('@', ',');
        var lexer = new Lexer(input);
        var parser = new Parser(lexer);
        var program = parser.parseProgram();

        assertEquals(0, parser.getErrors().size(), String.format("Errors found %s", parser.getErrors()));
        var got = program.toString();
        assertEquals(expected, got);
    }

    @ParameterizedTest
    @CsvSource({
            "5 + 5;, 5, +, 5",
            "5 - 5;, 5, -, 5",
            "5 * 5;, 5, *, 5",
            "5 / 5;, 5, /, 5",
            "5 > 5;, 5, >, 5",
            "5 < 5;, 5, <, 5",
            "5 == 5;, 5, ==, 5",
            "5 != 5;, 5, !=, 5",
    })
    void testParsingInfixExpression(String input, int leftValue, String operator, int rightValue) {
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();

        assertEquals(0, parser.getErrors().size(), String.format("Errors found %s", parser.getErrors()));
        assertEquals(1, program.getStatements().length);
        assertInstanceOf(ExpressionStatement.class, program.getStatements()[0]);
        ExpressionStatement stmt = (ExpressionStatement) program.getStatements()[0];
        testInfixExpression(stmt.getExpression(), leftValue, operator, rightValue);

    }

    @ParameterizedTest
    @CsvSource({
            "true == true, true, ==, true",
            "true != false, true, !=, false",
            "false == false, false, ==, false",
    })
    void testParsingInfixExpression(String input, boolean leftValue, String operator, boolean rightValue) {
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();

        assertEquals(0, parser.getErrors().size(), String.format("Errors found %s", parser.getErrors()));
        assertEquals(1, program.getStatements().length);
        assertInstanceOf(ExpressionStatement.class, program.getStatements()[0]);
        ExpressionStatement stmt = (ExpressionStatement) program.getStatements()[0];
        testInfixExpression(stmt.getExpression(), leftValue, operator, rightValue);
    }

    @ParameterizedTest
    @CsvSource({
            "true == true, true, ==, true",
            "true != false, true, !=, false",
            "false == false, false, ==, false"
    })
    void testParsingBooleanInfixExpression(String input, boolean leftValue, String operator, boolean rightValue) {
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();

        assertEquals(0, parser.getErrors().size(), String.format("Errors found %s", parser.getErrors()));
        assertEquals(1, program.getStatements().length);
        assertInstanceOf(ExpressionStatement.class, program.getStatements()[0]);
        ExpressionStatement stmt = (ExpressionStatement) program.getStatements()[0];
        testInfixExpression(stmt.getExpression(), leftValue, operator, rightValue);

    }

    @Test
    void testIfExpression() {
        String input = "if (x < y) { x } else { y }";

        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();

        assertEquals(0, parser.getErrors().size(), String.format("Errors found %s", parser.getErrors()));
        assertEquals(1, program.getStatements().length);
        assertInstanceOf(ExpressionStatement.class, program.getStatements()[0]);
        ExpressionStatement stmt = (ExpressionStatement) program.getStatements()[0];
        assertInstanceOf(IfExpression.class, stmt.getExpression());
        IfExpression exp = (IfExpression) stmt.getExpression();
        testInfixExpression(exp.getCondition(), "x", "<", "y");
        assertEquals(1, exp.getConsequence().getStatements().length);
        assertInstanceOf(ExpressionStatement.class, exp.getConsequence().getStatements()[0]);
        ExpressionStatement consequence = (ExpressionStatement) exp.getConsequence().getStatements()[0];
        testIdentifier(consequence.getExpression(), "x");
        assertNotNull(exp.getAlternative());
    }

    @Test
    void testFunctionLiteralParsing() {
        String input = "fn(x,y) {x+y;}";

        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();

        assertEquals(0, parser.getErrors().size(), String.format("Errors found %s", parser.getErrors()));
        assertEquals(1, program.getStatements().length);
        assertInstanceOf(ExpressionStatement.class, program.getStatements()[0]);
        ExpressionStatement stmt = (ExpressionStatement) program.getStatements()[0];
        assertInstanceOf(FunctionLiteral.class, stmt.getExpression());
        FunctionLiteral function = (FunctionLiteral) stmt.getExpression();
        assertEquals(2, function.getParameters().length);
        testLiteralExpression(function.getParameters()[0], "x");
        testLiteralExpression(function.getParameters()[1], "y");
        assertEquals(1, function.getBody().getStatements().length);
        assertInstanceOf(ExpressionStatement.class, function.getBody().getStatements()[0]);
        ExpressionStatement bodyStmt = (ExpressionStatement) function.getBody().getStatements()[0];
        testInfixExpression(bodyStmt.getExpression(), "x", "+", "y");
    }

    @ParameterizedTest
    @CsvSource({
            "fn(){};,",
            "fn(x) {};,x",
            "fn(x@y@z) {};,x y z"
    })
    void testFunctionParameterParsing(String input, String expected) {
        Lexer lexer = new Lexer(input.replace('@', ','));
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        String[] expectedParameters = Optional.ofNullable(expected).map(e -> e.split(" ")).orElse(new String[0]);
        assertEquals(0, parser.getErrors().size(), String.format("Errors found %s", parser.getErrors()));
        assertEquals(1, program.getStatements().length);
        assertInstanceOf(ExpressionStatement.class, program.getStatements()[0]);
        ExpressionStatement stmt = (ExpressionStatement) program.getStatements()[0];
        assertInstanceOf(FunctionLiteral.class, stmt.getExpression());
        FunctionLiteral function = (FunctionLiteral) stmt.getExpression();
        assertEquals(expectedParameters.length, function.getParameters().length);
        for (int i = 0; i < expectedParameters.length; i++) {
            testLiteralExpression(function.getParameters()[i], expectedParameters[i]);
        }
    }

    @Test
    void testCallExpressionParsing() {
        String input = "add(1,2*3,4+5);";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();

        assertEquals(0, parser.getErrors().size(), String.format("Errors found %s", parser.getErrors()));
        assertEquals(1, program.getStatements().length);
        assertInstanceOf(ExpressionStatement.class, program.getStatements()[0]);
        ExpressionStatement stmt = (ExpressionStatement) program.getStatements()[0];
        assertInstanceOf(CallExpression.class, stmt.getExpression());
        CallExpression exp = (CallExpression) stmt.getExpression();

        testIdentifier(exp.getFunction(), "add");
        assertEquals(3, exp.getArguments().length);

        testLiteralExpression(exp.getArguments()[0], 1);
        testInfixExpression(exp.getArguments()[1], 2, "*", 3);
        testInfixExpression(exp.getArguments()[2], 4, "+", 5);

    }

    @Test
    void testStringLiteralExpression() {
        String input = "\"hello world\"";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();

        assertEquals(0, parser.getErrors().size(), String.format("Errors found %s", parser.getErrors()));
        assertEquals(1, program.getStatements().length);
        assertInstanceOf(ExpressionStatement.class, program.getStatements()[0]);
        ExpressionStatement stmt = (ExpressionStatement) program.getStatements()[0];
        assertInstanceOf(StringLiteral.class, stmt.getExpression());
        StringLiteral stringLiteral = (StringLiteral) stmt.getExpression();
        assertEquals("hello world", stringLiteral.getValue());
    }

    @Test
    void testParsingArrayLiteral() {
        String input = "[1,2*2,3+3]";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();

        assertEquals(0, parser.getErrors().size(), String.format("Errors found %s", parser.getErrors()));
        assertEquals(1, program.getStatements().length);
        assertInstanceOf(ExpressionStatement.class, program.getStatements()[0]);
        ExpressionStatement stmt = (ExpressionStatement) program.getStatements()[0];
        assertInstanceOf(ArrayLiteral.class, stmt.getExpression());
        ArrayLiteral array = (ArrayLiteral) stmt.getExpression();
        assertEquals(3, array.getElements().length);

        testIntegerLiteral(array.getElements()[0], 1);
        testInfixExpression(array.getElements()[1], 2, "*", 2);
        testInfixExpression(array.getElements()[2], 3, "+", 3);
    }

    @Test
    void testParsingIndexExpression() {
        String input = "myArray[1+1];";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();

        assertEquals(0, parser.getErrors().size(), String.format("Errors found %s", parser.getErrors()));
        assertEquals(1, program.getStatements().length);
        assertInstanceOf(ExpressionStatement.class, program.getStatements()[0]);
        ExpressionStatement stmt = (ExpressionStatement) program.getStatements()[0];
        assertInstanceOf(IndexExpression.class, stmt.getExpression());
        IndexExpression index = (IndexExpression) stmt.getExpression();
        testIdentifier(index.getLeft(), "myArray");
        testInfixExpression(index.getIndex(), 1, "+", 1);
    }

    @Test
    void testParsingHashLiteralsStringKeys() {
        String input = "{\"one\":1,\"two\":2,\"three\":3}";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        assertEquals(0, parser.getErrors().size(), String.format("Errors found %s", parser.getErrors()));
        assertEquals(1, program.getStatements().length);
        assertInstanceOf(ExpressionStatement.class, program.getStatements()[0]);
        ExpressionStatement stmt = (ExpressionStatement) program.getStatements()[0];
        assertInstanceOf(HashLiteral.class, stmt.getExpression());
        HashLiteral hashLiteral = (HashLiteral) stmt.getExpression();
        assertEquals(3, hashLiteral.getPairs().size());
        Map<String, Integer> expected = Map.of("one", 1, "two", 2, "three", 3);

        for (Map.Entry<Expression, Expression> entry : hashLiteral.getPairs().entrySet()) {
            StringLiteral literal = (StringLiteral) entry.getKey();
            int expectedValue = expected.get(literal.getValue());
            testIntegerLiteral(entry.getValue(), expectedValue);
        }
    }

    @Test
    void testParsingEmptyHashLiteral() {
        String input = "{}";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        assertEquals(0, parser.getErrors().size(), String.format("Errors found %s", parser.getErrors()));
        assertEquals(1, program.getStatements().length);
        assertInstanceOf(ExpressionStatement.class, program.getStatements()[0]);
        ExpressionStatement stmt = (ExpressionStatement) program.getStatements()[0];
        HashLiteral hashLiteral = (HashLiteral) stmt.getExpression();
        assertEquals(0, hashLiteral.getPairs().size());
    }

    @Test
    void testParsingHashLiteralsWithExpression() {
        String input = "{\"one\":0+1,\"two\":10-8,\"three\":15/5}";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        assertEquals(0, parser.getErrors().size(), String.format("Errors found %s", parser.getErrors()));
        assertEquals(1, program.getStatements().length);
        assertInstanceOf(ExpressionStatement.class, program.getStatements()[0]);
        ExpressionStatement stmt = (ExpressionStatement) program.getStatements()[0];
        assertInstanceOf(HashLiteral.class, stmt.getExpression());
        HashLiteral hashLiteral = (HashLiteral) stmt.getExpression();
        assertEquals(3, hashLiteral.getPairs().size());

        Map<String, Function<Expression, Void>> expected = Map.of(
                "one", exp -> {
                    testInfixExpression(exp, 0, "+", 1);
                    return null;
                },
                "two", exp -> {
                    testInfixExpression(exp, 10, "-", 8);
                    return null;
                },
                "three", exp -> {
                    testInfixExpression(exp, 15,"/", 5);
                    return null;
                }
        );

        for (Map.Entry<Expression, Expression> entry : hashLiteral.getPairs().entrySet()) {
            StringLiteral literal = (StringLiteral) entry.getKey();
            expected.get(literal.getValue()).apply(entry.getValue());
        }
    }

    private void testLiteralExpression(Expression exp, Object expected) {
        if (expected instanceof Integer) {
            testIntegerLiteral(exp, (Integer) expected);
        } else if (expected instanceof String) {
            testIdentifier(exp, (String) expected);
        } else if (expected instanceof Boolean) {
            testBooleanLiteral(exp, (Boolean) expected);
        } else {
            fail();
        }
    }

    private void testBooleanLiteral(Expression exp, boolean value) {
        assertInstanceOf(interpreter.ast.Boolean.class, exp);
        var bo = (interpreter.ast.Boolean) exp;
        assertEquals(value, bo.isValue());
        assertEquals(Boolean.toString(value), bo.tokenLiteral());
    }

    private void testInfixExpression(Expression exp, Object left, String operator, Object right) {
        assertInstanceOf(InfixExpression.class, exp);
        var opExp = (InfixExpression) exp;
        testLiteralExpression(opExp.getLeft(), left);
        assertEquals(opExp.getOperator(), operator);
        testLiteralExpression(opExp.getRight(), right);
    }

    private void testIntegerLiteral(Expression il, int value) {
        IntegerLiteral integerLiteral = (IntegerLiteral) il;
        assertEquals(value, integerLiteral.getValue());
        assertEquals(Integer.toString(value), integerLiteral.tokenLiteral());
    }

    private void testLetStatement(Statement statement, String name) {
        assertEquals("let", statement.tokenLiteral());
        assertInstanceOf(LetStatement.class, statement);

        LetStatement letStatement = (LetStatement) statement;
        assertEquals(name, letStatement.getName().getValue());
        assertEquals(name, letStatement.getName().tokenLiteral());
    }

    private void testIdentifier(Expression exp, String value) {
        assertInstanceOf(Identifier.class, exp);
        var ident = (Identifier) exp;
        assertEquals(value, ident.getValue());
        assertEquals(value, ident.tokenLiteral());
    }
}
