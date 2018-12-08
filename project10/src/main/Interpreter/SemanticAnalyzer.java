package main.Interpreter;

import java.util.*;
import main.Lexer.TokenType;
import main.Scope.*;
import static main.Interpreter.StatementType.RETURN;
import static main.Lexer.TokenType.*;
import static main.Scope.VariableSymbol.Attribute;
import static main.Interpreter.StatementType.*;
import static main.Interpreter.StatementType.*;

/**
 * Perform semantic verification tasks relative to a specific scope.
 * Holds various tables used to determine valid token types or statements accepted by the sJava language specification.
 * Should be used when non-syntactic verification needs to be performed.
 */
class SemanticAnalyzer {

    /* Define which token types can be assigned to a variable. */
    private static final Map<TokenType, TokenType[]> assignmentMap = new HashMap<>();
    static{
        assignmentMap.put(INT, new TokenType[] {INT, ID});
        assignmentMap.put(BOOLEAN, new TokenType[] {INT, TRUE, FALSE, ID});
        assignmentMap.put(STRING_CONSTANT, new TokenType[] {STRING_CONSTANT, ID});
    }

    /* Define which token types are covariant with each other. */
    private static final Map<TokenType, TokenType[]> covariantTypeMap = new HashMap<>();
    static{
        covariantTypeMap.put(INT, new TokenType[] {INT});
        covariantTypeMap.put(DOUBLE, new TokenType[] {INT, DOUBLE});
        covariantTypeMap.put(BOOLEAN, new TokenType[] {INT, DOUBLE, BOOLEAN});
        covariantTypeMap.put(STRING, new TokenType[] {STRING});
        covariantTypeMap.put(CHAR, new TokenType[] {CHAR});
    }

    /* Define which are the valid statement types allowed inside a method block. */
    private static final StatementType[] validMethodStatements =
            new StatementType[] {VARIABLE_DECLARATION,
                    CONDITIONAL, ASSIGNMENT, RETURN, METHOD_CALLING, CLOSE_SCOPE};

    /* Define which are the valid statement types allowed in the global scope. */
    private static final StatementType[] validGlobalStatements =
            new StatementType[] {METHOD_DECLARATION, VARIABLE_DECLARATION, ASSIGNMENT};

    /* Define variable types allowed in an sJavac program. */
    static final TokenType[] variableTypes = new TokenType[] {INT, DOUBLE, BOOLEAN, CHAR, STRING};

    private Scope scope;

    /**
     * Constructs new semantic analyzer in relation to given scope.
     * @param scope scope being analyzed.
     */
    SemanticAnalyzer(Scope scope) {
        this.scope = scope;
    }

    /**
     * Validate the correct usage of a variable in the program.
     * Assert that variable is available in the current scope and initialized.
     * More over, assert that the variable is used correctly in relation to the valid type given
     * (e.g. String variable cannot be assign to an int).
     * @param validType the basis for the type comparison.
     * @param testedVariableName name of the variable to be tested.
     * @throws SemanticAnalyzerException if the verification failed, holds an informative message.
     */
    void verifyVariableUsage(TokenType validType, String testedVariableName) throws SemanticAnalyzerException{
        VariableSymbol varSymbol = getVariableIfDeclared(testedVariableName);
        requireInitialized(varSymbol);
        for (TokenType expectedType : covariantTypeMap.get(validType)) {
            if (varSymbol.getType() == expectedType) { return; }
        }
        throw new ContravariantVariableTypeException(validType, varSymbol.getType());
    }

    /* Assert that given variable symbol is marked as initialized. */
    private static void requireInitialized(VariableSymbol varSymbol) throws UninitializedVariableUsageException {
        if (!varSymbol.isActivated(Attribute.INITIALIZED)) {
            throw new UninitializedVariableUsageException();
        }
    }

    /**
     * Validate the assignment of a the variable given by the name assignedVariableName to assignTo variable.
     * In particular, verify that assignTo isn't marked as final, and check for correct variable usage.
     * @param assignTo VariableSymbol object represent the assignee.
     * @param assignedVariableName VariableSymbol object represent the object to be assigned.
     * @throws SemanticAnalyzerException if the verification failed, holds an informative message.
     */
    void verifyAssignment(VariableSymbol assignTo, String assignedVariableName) throws SemanticAnalyzerException {
        requireNonFinal(assignTo);
        verifyVariableUsage(assignTo.getType(), assignedVariableName);
    }

    /**
     * Verify that the given statement type is a valid statement inside a method block.
     * @param statement statement type to check.
     * @throws UnexpectedMethodStatementException if given statement is invalid.
     */
    static void verifyMethodStatement(StatementType statement) throws UnexpectedMethodStatementException {
        for (StatementType statementType : validMethodStatements) {
            if (statementType == statement) {
                return;
            }
        }
        throw new UnexpectedMethodStatementException(statement);
    }

    /**
     * Verify that the given statement type is a valid statement in relation to the global scope.
     * @param statement statement type to check.
     * @throws UnexpectedGlobalStatementException if given statement is invalid.
     */
    static void verifyGlobalStatement(StatementType statement) throws UnexpectedGlobalStatementException {
        for (StatementType statementType : validGlobalStatements) {
            if (statementType == statement) {
                return;
            }
        }
        throw new UnexpectedGlobalStatementException(statement);
    }

    /**
     * Get variable whose name is varName from the current scope, only if it declared.
     * @param varName variable identifier.
     * @return VariableSymbol for that variable.
     * @throws UndeclaredVariableUsageException if no symbol with the name varName was found in current scope or any
     * scope enclosing it.
     */
    VariableSymbol getVariableIfDeclared(String varName) throws UndeclaredVariableUsageException {
        try {
            return this.scope.lookupVariable(varName);
        } catch (NoSuchSymbolException e) {
            throw new UndeclaredVariableUsageException(varName);
        }
    }

    /** This method is in charge of validating that currTokenType matches one of the correct token types
     * allowed to be assigned to varType. */
    TokenType verifyAssignmentTokenType(TokenType varType, TokenType currTokenType)
            throws InvalidTokenForAssignment {
        for (TokenType expectedTokenType : assignmentMap.get(varType)) {
            if (currTokenType == expectedTokenType) {
                return expectedTokenType;
            }
        }
        throw new InvalidTokenForAssignment(currTokenType);
    }

    /** Assert that given variable symbol does not marked as final. */
    static void requireNonFinal(VariableSymbol varSymbol) throws CannotChangeFinalVariableException {
        if (varSymbol.isActivated(Attribute.FINAL)) {
            throw new CannotChangeFinalVariableException();
        }
    }
}
