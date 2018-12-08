package main.Lexer;

import java.util.regex.Pattern;

/**
 * This enum represent the known allowed tokens in a sJava file and their corresponding attributes.
 */
public enum TokenType {

    // Jack language symbols
    L_BRACE(Pattern.compile("\\{")),
    R_BRACE(Pattern.compile("}")),
    L_PAREN(Pattern.compile("\\(")),
    R_PAREN(Pattern.compile("\\)")),
    L_BOX_PAREN(Pattern.compile("\\[")),
    R_BOX_PAREN(Pattern.compile("\\]")),
    DOT(Pattern.compile("\\.")),
    COMMA(Pattern.compile(",")),
    SEMICOLON(Pattern.compile(";")),
    PLUS(Pattern.compile("\\+")),
    MINUS(Pattern.compile("-")),
    TIMES(Pattern.compile("\\*")),
    DIVIDER(Pattern.compile("\\\\")),
    AND(Pattern.compile("&")),
    OR(Pattern.compile("\\|")),
    LT(Pattern.compile("<")),
    GT(Pattern.compile(">")),
    EQ(Pattern.compile("=")),
    NOT(Pattern.compile("~")),

    // Jack language keywords
    CLASS(Pattern.compile("class(?!\\w)")),
    CONSTRUCTOR(Pattern.compile("constructor\\b")),
    FUNCTION(Pattern.compile("function\\b")),
    METHOD(Pattern.compile("method\\b")),
    FIELD(Pattern.compile("field\\b")),
    STATIC(Pattern.compile("static\\b")),
    VAR(Pattern.compile("var\\b")),
    INT(Pattern.compile("int\\b")),
    CHAR(Pattern.compile("char\\b")),
    BOOLEAN(Pattern.compile("boolean\\b")),
    VOID(Pattern.compile("void\\b")),
    TRUE(Pattern.compile("true(?!\\w)")),
    FALSE(Pattern.compile("false(?!\\w)")),
    NULL(Pattern.compile("null\\b")),
    THIS(Pattern.compile("this(?!\\w)")),
    LET(Pattern.compile("let\\b")),
    DO(Pattern.compile("do\\b")),
    IF(Pattern.compile("if(?:(?=\\()|(?=\\s))")),
    ELSE(Pattern.compile("else(?!\\w)")),
    WHILE(Pattern.compile("while(?:(?=\\()|(?=\\s))")),
    RETURN(Pattern.compile("return")),

    INTEGER_CONSTANT(Pattern.compile("-?\\d++(?!\\.)"), true), // Possessive quantifier is important
    STRING_CONSTANT(Pattern.compile("\"[^\"\\\\',]*\""), true),

    ID(Pattern.compile("(?:_\\w+)|(?:[a-zA-Z]\\w*)"), true);

    private Pattern pattern;
    private boolean isTokenValueExpected;

    /**
     * TokenType initialized with a pattern.
     * @param pattern the designated pattern
     */
    TokenType(Pattern pattern)
    {
        this.pattern = pattern;
        this.isTokenValueExpected = false;
    }

    /**
     * TokenType initialized with pattern and value expected constructor.
     * @param pattern the designated pattern
     * @param isTokenValueExpected true when value is expected. */
    TokenType(Pattern pattern, boolean isTokenValueExpected) {
        this.pattern = pattern;
        this.isTokenValueExpected = isTokenValueExpected;
    }

    /**
     * @return the pattern for the token type.
     */
    Pattern getPattern() {
        return this.pattern;
    }

    /**
     * @return true when value is expected for this token, false otherwise.
     */
    public boolean isTokenValueExpected() {
        return isTokenValueExpected;
    }
}
