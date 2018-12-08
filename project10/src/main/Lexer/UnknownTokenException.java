package main.Lexer;

public class UnknownTokenException extends RuntimeException {
    UnknownTokenException() {
        super("Unknown token found.");
    }
}
