package interpreter.repl;

import interpreter.ast.Program;
import interpreter.environment.Environment;
import interpreter.evaluator.Evaluator;
import interpreter.lexer.Lexer;
import interpreter.monkey.objects.MonkeyObject;
import interpreter.parser.Parser;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Repl {
    public static final String MONKEY_FACE =
                    "            __,__\n" +
                    "  .--.  .-\"     \"-.  .--.\n" +
                    " / .. \\/  .-. .-.  \\/ .. \\\n" +
                    "| |  '|  /   Y   \\  |'  | |\n" +
                    "| \\   \\  \\ 0 | 0 /  /   / |\n" +
                    " \\ '- ,\\.-\"`` ``\"-./, -' /\n" +
                    "  `'-' /_   ^ ^   _\\ '-'`\n" +
                    "      |  \\._ _ _./  |\n" +
                    "      \\   \\ ~ ~ /   /\n" +
                    "       '._ '-=-' _. '\n" +
                    "          '~---~'\n";

    public void start(InputStream in, OutputStream outIS) {
        Scanner scanner = new Scanner(in);
        PrintWriter out = new PrintWriter(outIS);
        out.println("Welcome to monkey repl");
        out.flush();
        String prompt = ">> ";
        Environment env = new Environment();
        while (true) {
            out.print(prompt);
            out.flush();
            String scanned = scanner.nextLine();
            if (scanned.equals("exit")) {
                scanner.close();
                return;
            }

            Lexer lexer = new Lexer(scanned);
            Parser parser = new Parser(lexer);
            Program program = parser.parseProgram();
            Evaluator eval = new Evaluator();

            if (!parser.getErrors().isEmpty()) {
                printParserErrors(out, parser.getErrors());
                continue;
            }

            var evaluated = eval
                    .eval(program, env);
            Optional.ofNullable(evaluated).map(MonkeyObject::inspect).ifPresent(out::println);
            out.flush();
        }
    }

    private void printParserErrors(PrintWriter out, List<String> errors) {
        out.println(MONKEY_FACE);
        out.println("Wooops! We ran into some monkey business");
        out.println("parser errors:");
        errors.forEach(err -> out.printf("\t %s \n", err));
    }
}
