package main.Interpreter;

/**
 * This enum holds all the statement types allowed in sJava program.
 */
public enum StatementType {
    CLASS_DECLERATION,
    CLASS_VAR_DEC,
    VAR_TYPE,
    SUBROUTINE_DEC,
    PARAMETER_LIST,
    SUBROUTINE_BODY,
    VAR_DEC,

    // todo: ??? needs?
    CLASS_NAME,
    SUBROUTINE_NAME,
    VAR_NAME,

    STATEMENT,
    LET_STATEMENT, // assignment
    IF_STATEMENT,
    WHILE_STATEMENT,
    DO_STATEMENT, // method calling
    RETURN_STATEMENT,

    EXPRESSION,
    TERM,
    SUBROUTINE_CALL,
    EXPRESSION_LIST,
    OP,
    UNARY_OP,
    KEYWORD_CONSTANT,

    OPEN_SCOPE,
    CLOSE_SCOPE
}
