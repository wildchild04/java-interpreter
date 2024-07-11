package interpreter.evaluator;

import interpreter.ast.ArrayLiteral;
import interpreter.environment.Environment;
import interpreter.lexer.Lexer;
import interpreter.monkey.objects.*;
import interpreter.parser.Parser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static interpreter.evaluator.Evaluator.*;
import static org.junit.jupiter.api.Assertions.*;


public class EvaluatorTest {

    private Evaluator eval;

    @BeforeEach
    void init() {
        eval = new Evaluator();
    }

    @ParameterizedTest
    @CsvSource({
            "5, 5",
            "10, 10",
            "-5, -5",
            "-10, -10",
            "5 + 5 + 5 + 5 - 10, 10",
            "2 * 2 * 2 * 2 * 2, 32",
            "-50 + 100 + -50, 0",
            "5 * 2 + 10, 20",
            "5 + 2 * 10, 25",
            "20 + 2 * -10, 0",
            "50 / 2 * 2 + 10, 60",
            "2 * (5 + 10), 30",
            "3 * 3 * 3 + 10, 37",
            "3 * (3 * 3) + 10, 37",
            "(5 + 10 * 2 + 15 / 3) * 2 + -10, 50"
    })
    void testEvalIntegerExpression(String input, int expected) {
        MonkeyObject evaluated = testEval(input);
        assertNotNull(evaluated);
        testIntegerObject(evaluated, expected);
    }

    @ParameterizedTest
    @CsvSource({
            "true, true",
            "false, false",
            "1 < 2, true",
            "1 > 2, false",
            "1 < 1, false",
            "1 > 1, false",
            "1 == 1, true",
            "1 != 1, false",
            "1 == 2, false",
            "1 != 2, true",
            "true == true, true",
            "false == false, true",
            "true == false, false",
            "true != false, true",
            "false != true, true",
            "(1 < 2) == true, true",
            "(1 < 2) == false, false",
            "(1 > 2) == true, false",
            "(1 > 2) == false, true"
    })
    void testEvalBooleanExpression(String input, boolean expected) {
        MonkeyObject evaluated = testEval(input);
        testBooleanObject(evaluated, expected);
    }

    @ParameterizedTest
    @CsvSource({
            "!true,false",
            "!false,true",
            "!5,false",
            "!!true,true",
            "!!false, false",
            "!!5,true"
    })
    void testBangOperator(String input, boolean expected) {

        MonkeyObject evaluated = testEval(input);
        testBooleanObject(evaluated, expected);
    }

    @ParameterizedTest
    @CsvSource({
            "if(true) { 10 }, int, 10",
            "if(false) { 10 }, null, null",
            "if(1) { 10 }, int, 10",
            "if(1 < 2) { 10 }, int, 10",
            "if(1 > 2) { 10 }, null, null",
            "if(1 > 2) { 10 } else { 20 }, int, 20",
            "if(1 < 2) { 10 } else { 20 }, int, 10"
    })
    void testIfElseExpression(String input, String expType, String expected) {
        MonkeyObject evaluated = testEval(input);

        switch (expType) {
            case "int" -> testIntegerObject(evaluated, Integer.parseInt(expected));
            case "null" -> testNullObject(evaluated);
        }
    }

    @ParameterizedTest
    @CsvSource({
            "return 10;,10",
            "return 10;9;,10",
            "return 2*5;9;,10",
            "9; return 2*5;9;,10",
            "if(10>1){if(10>1){return 10;}return 1;},10",
    })
    void testReturnStatements(String input, int expected) {
        MonkeyObject evaluated = testEval(input);
        testIntegerObject(evaluated, expected);
    }

    @ParameterizedTest
    @CsvSource({
            "5 + true;, type mismatch: INTEGER + BOOLEAN",
            "5 + true; 5;, type mismatch: INTEGER + BOOLEAN",
            "-true, unknown operator: -BOOLEAN",
            "true + false;, unknown operator: BOOLEAN + BOOLEAN",
            "5; true + false; 5, unknown operator: BOOLEAN + BOOLEAN",
            "if (10 > 1) { true + false; }, unknown operator: BOOLEAN + BOOLEAN",
            "if (10 > 1) { if (10 > 1) { return true + false; } return 1; }, unknown operator: BOOLEAN + BOOLEAN",
            "foobar,Identifier not found: foobar",
            "\"Hello\" - \"World\",unknown operator: STRING - STRING",
            "{\" name\": \"Monkey\"}[fn( x) { x }];,unusable as hash key: FUNCTION",
})
    void testErrorHandling(String input, String expectedMessage) {

        MonkeyObject evaluated = testEval(input);
        assertInstanceOf(MonkeyError.class, evaluated);
        MonkeyError monkeyError = (MonkeyError) evaluated;
        assertEquals(expectedMessage, monkeyError.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "let a=5;a;,5",
            "let a=5*5; a;,25",
            "let a=5;let b=a;b;,5",
            "let a=5;let b=a;let c=a+b+5;c;,15"
    })
    void testLetStatements(String input, int expected) {
        Evaluator eval = new Evaluator();
        MonkeyObject evaluated = testEval(input);

        testIntegerObject(evaluated, expected);
    }

    @Test
    void testFunctionObject() {
        String input = "fn(x){x+2;};";

        MonkeyObject evaluated = testEval(input);
        assertInstanceOf(MonkeyFunction.class, evaluated);
        MonkeyFunction function = (MonkeyFunction) evaluated;

        assertEquals(1, function.getParameters().length);
        String expectBody = "(x + 2)";
        assertEquals(expectBody, function.getBody().toString());
    }

    @ParameterizedTest
    @CsvSource({
            "let identity = fn(x) {x;}; identity(5);, 5",
            "let identity = fn(x) { return x; }; identity(5);, 5",
            "let double = fn(x) { x * 2; }; double(5);, 10",
            "let add = fn(x@ y) { x + y; }; add(5@ 5);, 10",
            "let add = fn(x@ y) { x + y; }; add(5 + 5@ add(5@ 5));, 20",
            "fn(x) { x; }(5), 5"
    })
    void testFunctionApplication(String input, int expected) {
        input = input.replace('@', ',');
        testIntegerObject(testEval(input), expected);
    }

    @Test
    void testStringLiteral() {
        String input = "\"Hello World!\"";
        MonkeyObject evaluated = testEval(input);
        assertInstanceOf(MonkeyString.class, evaluated);
        MonkeyString string = (MonkeyString) evaluated;
        assertEquals(string.getValue(), "Hello World!");
    }

    @Test
    void testStringConcatenation() {
        String input = "\"Hello\" + \" World!\"";
        MonkeyObject evaluated = testEval(input);
        assertInstanceOf(MonkeyString.class, evaluated);
        MonkeyString string = (MonkeyString) evaluated;
        assertEquals(string.getValue(), "Hello World!");
    }

    @ParameterizedTest
    @CsvSource({
            "len(\"\"),int, 0",
            "len(\"four\"),int, 4",
            "len(\"hello world\"),int, 11",
            "len(1), string, argument to `len` not supported@ got INTEGER",
            "len(\"one\"@ \"two\"),string, wrong number of arguments. got=2@ want=1"
    })
    void testBuiltinFunction(String input, String type, String expected) {
        input = input.replaceAll("@", ",");
        expected = expected.replaceAll("@", ",");
        MonkeyObject evaluated = testEval(input);

        switch (type) {
            case "int" -> testIntegerObject(evaluated, Integer.parseInt(expected));
            case "string" -> {
                assertInstanceOf(MonkeyError.class, evaluated);
                MonkeyError err = (MonkeyError) evaluated;
                assertEquals(expected, err.getMessage());
            }
        }
    }

    @Test
    void testArrayLiterals() {
        String input = "[1,2*2,3+3]";
        MonkeyObject evaluated = testEval(input);

        assertInstanceOf(MonkeyArray.class, evaluated);
        MonkeyArray array = (MonkeyArray) evaluated;
        assertEquals(3, array.getElements().length);

        testIntegerObject(array.getElements()[0], 1);
        testIntegerObject(array.getElements()[1], 4);
        testIntegerObject(array.getElements()[2], 6);
    }

    @ParameterizedTest
    @CsvSource({
            "[1@2@3][0], 1",
            "[1@2@3][1], 2",
            "[1@2@3][2], 3",
            "let i = 0; [1][i];, 1",
            "[1@2@3][1 + 1], 3",
            "let myArray = [1@ 2@ 3]; myArray[2];, 3",
            "let myArray = [1@ 2@ 3]; myArray[0] + myArray[1] + myArray[2];, 6",
            "let myArray = [1@ 2@ 3]; let i = myArray[0]; myArray[i], 2",
            "[1@2@3][3], null",
            "[1@2@3][-1], null"
    })
    void testArrayIndexExpression(String input, String expected) {
        input = input.replaceAll("@", ",");
        expected = expected.replaceAll("@", ",");
        MonkeyObject evaluated = testEval(input);
        if (expected.equals("null")) {
            testNullObject(evaluated);
        } else {
            testIntegerObject(evaluated, Integer.parseInt(expected));
        }
    }

    @Test
    void testHashLiterals() {
        String input = """
                let two = "two";
                {
                    "one": 10-9,
                    "two": 1+1,
                    "thr"+"ee": 6/2,
                    4:4,
                    true:5,
                    false:6
                }
                """;
        MonkeyObject evaluated = testEval(input);
        assertInstanceOf(MonkeyHash.class, evaluated);
        MonkeyHash hash = (MonkeyHash) evaluated;
        Map<MonkeyHashKey, Long> expected = Map.of(
                new MonkeyString("one").hashKey(), 1L,
                new MonkeyString("two").hashKey(), 2L,
                new MonkeyString("three").hashKey(), 3L,
                new MonkeyInteger(4).hashKey(), 4L,
                TRUE.hashKey(), 5L,
                FALSE.hashKey(), 6L
        );

        assertEquals(expected.size(), hash.getPairs().size());
        expected.entrySet().forEach(entry -> {
            MonkeyHashPair pair = hash.getPairs().get(entry.getKey());
            testIntegerObject(pair.getValue(), Math.toIntExact(entry.getValue()));
        });
    }

    @ParameterizedTest
    @CsvSource({
            "{\"foo\":5}[\"foo\"], int, 5",
            "{\"foo\":5}[\"bar\"], null, null",
            "{\"foo\": 5}[\"foo\"], int, 5",
            "{\"foo\": 5}[\"bar\"], null, null",
            "let key = \"foo\"; {\"foo\": 5}[key], int, 5",
            "{}[\"foo\"], null, null",
            "{5: 5}[5], int, 5",
            "{true: 5}[true], int, 5",
            "{false: 5}[false], int, 5"
    })
    void testHashIndexExpression(String input, String type, String expected) {
        MonkeyObject evaluated = testEval(input);
        if (type.equals("null")) {
            testNullObject(evaluated);
        } else {
            testIntegerObject(evaluated, Integer.parseInt(expected));
        }
    }

    private void testNullObject(MonkeyObject evaluated) {
        assertEquals(NULL, evaluated);
    }

    private void testBooleanObject(MonkeyObject evaluated, boolean expected) {
        assertNotNull(evaluated);
        assertInstanceOf(MonkeyBoolean.class, evaluated);
        MonkeyBoolean bool = (MonkeyBoolean) evaluated;
        assertEquals(expected, bool.isValue());
    }

    private MonkeyObject testEval(String input) {
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Environment env = new Environment();
        Evaluator eval = new Evaluator();
        return eval.eval(parser.parseProgram(), env);
    }

    private void testIntegerObject(MonkeyObject object, int expected) {
        MonkeyInteger integer = null;
        if (object instanceof MonkeyReturnValue) {
            integer = (MonkeyInteger) ((MonkeyReturnValue) object).getValue();
        } else if (object instanceof MonkeyInteger) {
            integer = (MonkeyInteger) object;
        } else {
            fail("Monkey object is not integer nor return value " + object.type());
        }

        assertEquals(expected, integer.getValue());
    }
}
