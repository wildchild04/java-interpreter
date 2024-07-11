package interpreter.evaluator;

import interpreter.ast.Expression;
import interpreter.monkey.objects.*;

import java.util.Arrays;
import java.util.Map;

import static interpreter.evaluator.Evaluator.NULL;

public class BuiltInFns {

    public static Map<String, MonkeyBuiltin> BUILTIN_FNS = Map.of(
            "len", new MonkeyBuiltin((args) -> {
                if (args.length != 1) {
                    return new MonkeyError(String.format("wrong number of arguments. got=%d, want=1", args.length));
                }

                if (args[0] instanceof MonkeyString) {
                    MonkeyString string = (MonkeyString) args[0];
                    return new MonkeyInteger(string.getValue().length());
                }

                if (args[0] instanceof MonkeyArray) {
                    MonkeyArray arr = (MonkeyArray) args[0];
                    return new MonkeyInteger(arr.getElements().length);
                }

                return new MonkeyError(String.format("argument to `len` not supported, got %s", args[0].type()));
            }),
            "first", new MonkeyBuiltin((args) -> {
                if (args.length != 1) {
                    return new MonkeyError(String.format("wrong number of arguments. got=%d, want=1", args.length));
                }
                if (!args[0].type().equals(MonkeyArray.ARRAY_OBJ)) {
                    return new MonkeyError("argument to `first` must be ARRAY, got " + args[0].type());
                }
                MonkeyArray arr = (MonkeyArray) args[0];

                if (arr.getElements().length > 0) {
                    return arr.getElements()[0];
                }

                return NULL;
            }),
            "last", new MonkeyBuiltin((args) -> {
                if (args.length != 1) {
                    return new MonkeyError(String.format("wrong number of arguments. got=%d, want=1", args.length));
                }
                if (!args[0].type().equals(MonkeyArray.ARRAY_OBJ)) {
                    return new MonkeyError("argument to `first` must be ARRAY, got " + args[0].type());
                }
                MonkeyArray arr = (MonkeyArray) args[0];

                if (arr.getElements().length > 0) {
                    return arr.getElements()[arr.getElements().length-1];
                }

                return NULL;
            }),
            "rest", new MonkeyBuiltin((args) -> {
                if (args.length != 1) {
                    return new MonkeyError(String.format("wrong number of arguments. got=%d, want=1", args.length));
                }
                if (!args[0].type().equals(MonkeyArray.ARRAY_OBJ)) {
                    return new MonkeyError("argument to `first` must be ARRAY, got " + args[0].type());
                }
                MonkeyArray arr = (MonkeyArray) args[0];

                if (arr.getElements().length > 0) {
                    MonkeyObject[] rest = Arrays.copyOfRange(arr.getElements(),1,arr.getElements().length);
                    return new MonkeyArray(rest);
                }

                return NULL;
            }),
            "push", new MonkeyBuiltin((args) -> {
                if (args.length != 2) {
                    return new MonkeyError(String.format("wrong number of arguments. got=%d, want=2", args.length));
                }
                if (!args[0].type().equals(MonkeyArray.ARRAY_OBJ)) {
                    return new MonkeyError("argument to `first` must be ARRAY, got " + args[0].type());
                }
                MonkeyArray arr = (MonkeyArray) args[0];

                MonkeyObject[] newArray = Arrays.copyOf(arr.getElements(), arr.getElements().length+1);
                newArray[arr.getElements().length]= args[1];

                return new MonkeyArray(newArray);
            }),
            "puts", new MonkeyBuiltin((args) -> {
                Arrays.stream(args).map(MonkeyObject::inspect).forEach(System.out::println);
                return NULL;
            })
    );
}