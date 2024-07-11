package interpreter.evaluator;

import interpreter.ast.Boolean;
import interpreter.ast.*;
import interpreter.environment.Environment;
import interpreter.monkey.objects.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

public class Evaluator {

    public static MonkeyBoolean TRUE = new MonkeyBoolean(true);
    public static MonkeyBoolean FALSE = new MonkeyBoolean(false);
    public static MonkeyNull NULL = new MonkeyNull();

    public MonkeyObject eval(Node node, Environment env) {

        switch (node.type()) {
            case PROGRAM -> {
                return evalProgram((Program) node, env);
            }
            case EXPRESSION_STATEMENT -> {
                return eval(((ExpressionStatement) node).getExpression(), env);
            }
            case INTEGER -> {
                return new MonkeyInteger(((IntegerLiteral) node).getValue());
            }
            case BOOLEAN -> {
                return ((Boolean) node).isValue() ? TRUE : FALSE;
            }
            case NULL -> {
                return NULL;
            }
            case PREFIX_EXPRESSION -> {
                PrefixExpression prefixExpression = (PrefixExpression) node;
                MonkeyObject right = eval(prefixExpression.getRight(), env);
                if (isError(right)) {
                    return right;
                }
                return evalPrefixExpression(prefixExpression.getOperator(), right);
            }
            case INFIX_EXPRESSION -> {
                InfixExpression exp = (InfixExpression) node;
                MonkeyObject left = eval(exp.getLeft(), env);
                if (isError(left)) {
                    return left;
                }
                MonkeyObject right = eval(exp.getRight(), env);
                if (isError(right)) {
                    return right;
                }
                return evalInfixExpression(exp.getOperator(), left, right);
            }
            case BLOCK_STATEMENT -> {
                return evalBLockStatement((BlockStatement) node, env);
            }
            case IF_EXPRESSION -> {
                return evalIfExpression((IfExpression) node, env);
            }
            case RETURN_STATEMENT -> {
                ReturnStatement returnVal = (ReturnStatement) node;
                MonkeyObject val = eval(returnVal.getReturnValue(), env);
                if (isError(val)) {
                    return val;
                }
                return new MonkeyReturnValue(val);
            }
            case LET_STATEMENT -> {
                MonkeyObject val = eval(((LetStatement) node).getValue(), env);
                if (isError(val)) {
                    return val;
                }
                env.set(((LetStatement) node).getName().getValue(), val);

            }
            case IDENTIFIER -> {
                return evalIdentifier((Identifier) node, env);
            }

            case FUNCTION_LITERAL -> {
                FunctionLiteral function = (FunctionLiteral) node;
                Identifier[] params = function.getParameters();
                BlockStatement body = function.getBody();
                return new MonkeyFunction(params, body, env);
            }

            case CALL_EXPRESSION -> {

                MonkeyObject function = eval(((CallExpression) node).getFunction(), env);
                if (isError(function)) {
                    return function;
                }
                MonkeyObject[] args = evalExpressions(((CallExpression) node).getArguments(), env);
                if (args.length == 1 && isError(args[0])) {
                    return args[0];
                }

                return applyFunction(function, args);
            }
            case STRING -> {
                return new MonkeyString(((StringLiteral) node).getValue());
            }

            case ARRAY_LIST -> {
                ArrayLiteral arrayLiteral = (ArrayLiteral) node;
                MonkeyObject[] elements = evalExpressions(arrayLiteral.getElements(), env);
                if (elements.length == 1 && isError(elements[0])) {
                    return elements[0];
                }

                return new MonkeyArray(elements);
            }

            case INDEX_EXPRESSION -> {
                IndexExpression indexExpression = (IndexExpression) node;
                MonkeyObject left = eval(indexExpression.getLeft(), env);
                if (isError(left)) {
                    return left;
                }
                MonkeyObject index = eval(indexExpression.getIndex(), env);
                if (isError(index)) {
                    return index;
                }

                return evalIndexExpression(left, index);
            }

            case HASH -> {
                return evalHashLiteral((HashLiteral) node, env);
            }


        }

        return null;
    }

    private MonkeyObject evalHashLiteral(HashLiteral node, Environment env) {
        Map<MonkeyHashKey, MonkeyHashPair> pairs = new HashMap<>();

        for (var entry : node.getPairs().entrySet()) {
            MonkeyObject key = eval(entry.getKey(), env);
            if (isError(key)) {
                return key;
            }
            Hashable hashKey = (Hashable) key;

            MonkeyObject val = eval(entry.getValue(), env);
            if (isError(val)) {
                return val;
            }
            MonkeyHashKey hashed = hashKey.hashKey();
            pairs.put(hashed, new MonkeyHashPair(key, val));
        }
        return new MonkeyHash(pairs);
    }

    private MonkeyObject evalIndexExpression(MonkeyObject left, MonkeyObject index) {
        if (left.type().equals(MonkeyArray.ARRAY_OBJ) && index.type().equals(MonkeyInteger.MONKEY_INT)) {
            return evalArrayIndexExpression(left, index);
        }
        if (left.type().equals(MonkeyHash.HASH_OBJ)) {
            return evalHashIndexExpression(left, index);
        }

        return new MonkeyError("index operator not supported: " + left.type());
    }

    private MonkeyObject evalHashIndexExpression(MonkeyObject left, MonkeyObject index) {
        MonkeyHash hash = (MonkeyHash) left;
        if (!(index instanceof Hashable)) {
            return new MonkeyError("unusable as hash key: " + index.type());
        }
        Hashable key = (Hashable) index;

        MonkeyHashPair pair = hash.getPairs().get(key.hashKey());
        return pair != null ? pair.getValue() : NULL;
    }

    private MonkeyObject evalArrayIndexExpression(MonkeyObject left, MonkeyObject index) {
        MonkeyArray array = (MonkeyArray) left;
        MonkeyInteger idx = (MonkeyInteger) index;
        int max = array.getElements().length;
        if (idx.getValue() < 0 || idx.getValue() >= max) {
            return NULL;
        }

        return array.getElements()[idx.getValue()];
    }

    private MonkeyObject applyFunction(MonkeyObject fn, MonkeyObject[] args) {
        if (fn instanceof MonkeyFunction) {
            MonkeyFunction function = (MonkeyFunction) fn;
            Environment extendedEnv = extendedFunctionEnv(function, args);
            MonkeyObject evaluated = eval(function.getBody(), extendedEnv);
            return unwrapReturnValue(evaluated);
        }

        if (fn instanceof MonkeyBuiltin) {
            MonkeyBuiltin builtin = (MonkeyBuiltin) fn;
            return builtin.getBuiltinFunction().apply(args);
        }

        return new MonkeyError("not a function: " + fn.type());

    }

    private MonkeyObject unwrapReturnValue(MonkeyObject object) {
        if (object instanceof MonkeyReturnValue) {
            return ((MonkeyReturnValue) object).getValue();
        }

        return object;
    }

    private Environment extendedFunctionEnv(MonkeyFunction function, MonkeyObject[] args) {
        Environment env = new Environment(function.getEnv());

        for (int i = 0; i < function.getParameters().length; i++) {
            env.set(function.getParameters()[i].getValue(), args[i]);
        }

        return env;
    }

    private MonkeyObject[] evalExpressions(Expression[] expressions, Environment env) {
        LinkedList<MonkeyObject> res = new LinkedList<>();

        if (expressions != null) {

            for (Expression exp : expressions) {
                MonkeyObject evaluated = eval(exp, env);
                if (isError(evaluated)) {
                    return new MonkeyObject[]{evaluated};
                }
                res.add(evaluated);
            }
        }
        return res.toArray(new MonkeyObject[0]);
    }

    private MonkeyObject evalIdentifier(Identifier node, Environment env) {
        MonkeyObject val = env.get(node.getValue()).orElse(BuiltInFns.BUILTIN_FNS.get(node.getValue()));

        return Optional.ofNullable(val).orElse(new MonkeyError("Identifier not found: " + node.getValue()));
    }

    private MonkeyObject evalBLockStatement(BlockStatement block, Environment env) {
        MonkeyObject result = null;

        for (Statement statement : block.getStatements()) {
            result = eval(statement, env);
            if (result == null) {
                continue;
            }
            if (MonkeyReturnValue.RETURN_VALUE_OBJ.equals(result.type())) {
                return result;
            }

            if (MonkeyError.ERROR_OBJ.equals(result.type())) {
                return result;
            }
        }
        return result;
    }

    private MonkeyObject evalProgram(Program program, Environment env) {
        MonkeyObject result = null;

        for (Statement stmt : program.getStatements()) {
            result = eval(stmt, env);
            if (result == null) {
                continue;
            }

            if (MonkeyReturnValue.RETURN_VALUE_OBJ.equals(result.type())) {
                return ((MonkeyReturnValue) result).getValue();
            }

            if (MonkeyError.ERROR_OBJ.equals(result.type())) {
                return result;
            }
        }
        return result;
    }

    private MonkeyObject evalIfExpression(IfExpression ifExpression, Environment env) {
        MonkeyObject condition = eval(ifExpression.getCondition(), env);
        if (isError(condition)) {
            return condition;
        }
        if (isTruthy(condition)) {
            return eval(ifExpression.getConsequence(), env);
        } else if (ifExpression.getAlternative() != null) {
            return eval(ifExpression.getAlternative(), env);
        } else {
            return NULL;
        }
    }

    private boolean isTruthy(MonkeyObject condition) {
        if (condition == NULL) {
            return false;
        }
        if (condition == TRUE) {
            return true;
        }
        if (condition == FALSE) {
            return false;
        }

        return true;
    }

    private MonkeyObject evalInfixExpression(String operator, MonkeyObject left, MonkeyObject right) {
        if (MonkeyInteger.MONKEY_INT.equals(left.type()) && MonkeyInteger.MONKEY_INT.equals(right.type())) {
            return evalIntegerInfixExpression(operator, left, right);
        }

        if ("==".equals(operator)) {
            boolean leftVal = ((MonkeyBoolean) left).isValue();
            boolean rightVal = ((MonkeyBoolean) right).isValue();
            return leftVal == rightVal ? TRUE : FALSE;
        }

        if ("!=".equals(operator)) {
            boolean leftVal = ((MonkeyBoolean) left).isValue();
            boolean rightVal = ((MonkeyBoolean) right).isValue();
            return leftVal != rightVal ? TRUE : FALSE;
        }

        if (!left.type().equals(right.type())) {
            return new MonkeyError(String.format("type mismatch: %s %s %s", left.type(), operator, right.type()));
        }

        if (left.type().equals(MonkeyString.STRING_OBJ) && left.type().equals(MonkeyString.STRING_OBJ)) {
            return evalStringInfixExpression(operator, left, right);
        }

        return new MonkeyError(String.format("unknown operator: %s %s %s", left.type(), operator, right.type()));
    }

    private MonkeyObject evalStringInfixExpression(String operator, MonkeyObject left, MonkeyObject right) {
        if (!operator.equals("+")) {
            return new MonkeyError(String.format("unknown operator: %s %s %s", left.type(), operator, right.type()));
        }
        MonkeyString leftVal = (MonkeyString) left;
        MonkeyString rightVal = (MonkeyString) right;
        return new MonkeyString(leftVal.getValue() + rightVal.getValue());
    }

    private MonkeyObject evalIntegerInfixExpression(String operator, MonkeyObject left, MonkeyObject right) {
        int leftValue = ((MonkeyInteger) left).getValue();
        int rightValue = ((MonkeyInteger) right).getValue();

        switch (operator) {
            case "+" -> {
                return new MonkeyInteger(leftValue + rightValue);
            }
            case "-" -> {
                return new MonkeyInteger(leftValue - rightValue);
            }
            case "/" -> {
                return new MonkeyInteger(leftValue / rightValue);
            }
            case "*" -> {
                return new MonkeyInteger(leftValue * rightValue);
            }
            case "<" -> {
                return leftValue < rightValue ? TRUE : FALSE;
            }
            case ">" -> {
                return leftValue > rightValue ? TRUE : FALSE;
            }
            case "==" -> {
                return leftValue == rightValue ? TRUE : FALSE;
            }
            case "!=" -> {
                return leftValue != rightValue ? TRUE : FALSE;
            }
            default -> {
                return new MonkeyError(String.format("unknown operator: %s %s %s", left.type(), operator, right.type()));
            }
        }
    }


    private MonkeyObject evalPrefixExpression(String operator, MonkeyObject right) {
        switch (operator) {
            case "!" -> {
                return evalBangOperatorExpression(right);
            }
            case "-" -> {
                return evalMinusPrefixOperatorExpression(right);
            }
            default -> {
                return new MonkeyError(String.format("unknown operator: %s%s", operator, right.type()));
            }
        }
    }

    private MonkeyObject evalMinusPrefixOperatorExpression(MonkeyObject right) {
        if (!(MonkeyInteger.MONKEY_INT.equals(right.type()))) {
            return new MonkeyError(String.format("unknown operator: -%s", right.type()));
        }

        int value = ((MonkeyInteger) right).getValue();
        return new MonkeyInteger(-value);
    }

    private MonkeyObject evalBangOperatorExpression(MonkeyObject right) {
        if (TRUE.equals(right)) {
            return FALSE;
        }
        if (FALSE.equals(right)) {
            return TRUE;
        }
        if (NULL.equals(right)) {
            return TRUE;
        }

        return FALSE;
    }

    private MonkeyObject evalStatements(Statement[] statements, Environment env) {
        MonkeyObject result = null;

        for (Statement statement : statements) {
            result = eval(statement, env);

            if (result instanceof MonkeyReturnValue) {
                return ((MonkeyReturnValue) result).getValue();
            }
        }

        return result;
    }

    private boolean isError(MonkeyObject obj) {
        return Optional.ofNullable(obj).map(o -> o.type().equals(MonkeyError.ERROR_OBJ)).orElse(false);
    }
}
