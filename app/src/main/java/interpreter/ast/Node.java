package interpreter.ast;

public interface Node {
    String tokenLiteral();
    NodeType type();
}
