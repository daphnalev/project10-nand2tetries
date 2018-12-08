package main.Lexer;

/**
 * This class represents a Token type object.
 */
public class Token {

    private String value;
    private TokenType type;

    /**
     * Constructs new Token object with given type.
     * @param type The type of the token.
     */
    Token(TokenType type) {
        this.type = type;
    }

    /**
     * Constructs new Token object with type and value.
     * @param type The type of the token.
     * @param value The value of the token.
     */
    Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    /**
     * @return the type of the token.
     */
    public TokenType getType() {
        return type;
    }

    /**
     * @return The value of the token.
     */
    public String getValue() {
        return value;
    }
}
