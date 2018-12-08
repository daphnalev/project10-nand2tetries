package main.Lexer;

import java.util.*;
import java.util.regex.*;

/**
 * This class generate a stream of tokens for given lines.
 */
public class Tokenizer implements Iterator<Token>, Iterable<Token> {

    private static final String LINE_COMMENT_REGEX = "^//.+";
    private static final String ILLEGAL_METHOD_PREFIX = "_";

    private String line;
    private int position;

    /**
     * Create a new Tokenizer iterator object.
     * @param line the given line to iterate over.
     */
    public Tokenizer(String line) {
        this.line = line;
        this.position = 0;
        // todo - add /* comments.
        if (isComment(line)) { // line comments define empty iterator.
            position = line.length();
        } else {
            skipWhitespace();
        }
    }

    @Override
    public boolean hasNext() {
        return position < line.length();
    }

    @Override
    public Token next() {
        Token currToken = getTokenFromPosition();
        skipWhitespace();
        return currToken;
    }

    /* This method gets the matched token from the current position of the iterator. */
    private Token getTokenFromPosition() {
        int prev_position = position;
        // Iterate through available TokenTypes and check if matches remained line
        for(TokenType tokenType : TokenType.values()) {
            if (match(tokenType.getPattern())) {
                return createTokenForMatchedPattern(prev_position, tokenType);
            }
        }
        position = line.length();
        throw new UnknownTokenException();
    }

    /* This method creates a token for the matched pattern. */
    private Token createTokenForMatchedPattern(int prev_position, TokenType tokenType) {
        if (tokenType.isTokenValueExpected()) {
            return new Token(tokenType, line.substring(prev_position, position));
        } else {
            return new Token(tokenType);
        }
    }

    /* This methods moves the iterator position to the next char that isn't a whitespace. */
    private void skipWhitespace() {
        while (position < line.length() &&
                Character.isWhitespace(line.charAt(position))) {
            position++;
        }
    }

    /* This function checks the given pattern to the line position given. */
    private boolean match(Pattern pattern) {
        Matcher matcher = pattern.matcher(line);
        matcher.region(position, line.length());
        if (matcher.lookingAt()) {
            position = matcher.end();
            return true;
        }
        return false;
    }

    @Override
    public Iterator<Token> iterator() {
        return this;
    }

    /**
     * This function validates that the method's name doesn't start with ILLEGAL_METHOD_PREFIX.
     * Otherwise, throws an exception.
     * @param methodName The name of the method to check.
     */
    public static void validateMethodName(String methodName) throws UnknownTokenException {
        if (methodName.startsWith(ILLEGAL_METHOD_PREFIX)) {
            throw new UnknownTokenException();
        }
    }

    /**
     * Determine whether the given line represent a valid comment.
     * @param line input to check.
     * @return true if and only if given line represent a valid comment.
     */
    public static boolean isComment(String line) {
        return line.matches(LINE_COMMENT_REGEX);
    }

}
