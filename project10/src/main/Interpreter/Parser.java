package main.Interpreter;

import static main.Lexer.TokenType.*;
import static main.Scope.VariableSymbol.Attribute;
import static main.Scope.VariableSymbol.Attribute;
import main.Lexer.*;
import main.Scope.*;

import java.util.*;
import static java.util.Objects.*;

/**
 * Analyze a line of input and determines whether the line is syntactically correct, as well as semantically correct.
 */
public class Parser {

    private Iterator<Token> tokensIterator;
    private SemanticAnalyzer semanticAnalyzer;
    private Scope scope;
    private Token currToken;
    private Token nextToken;

    /**
     * Create a new parser object in relation to the given scope.
     * @param scope the scope of the line (the methods and parameters known).
     */
    public Parser (Scope scope) {
        this.scope = scope;
        this.semanticAnalyzer = new SemanticAnalyzer(scope);
    }

    /**
     * Main parsing method. Validate that the statement is syntactically correct and semantically correct.
     * Updates the scope according to the type of parsed statement.
     * @param line line to parse.
     * @return null if and only if line has no tokens; else, return the statement type of the line just
     * parsed.
     */
    public StatementType parse(String line) {
        initParser(line);
        if (currToken == null) { return null; }
        // Decide which type of statement is currently parsed.
        StatementType statement = StatementFactory.getStatement(currToken, nextToken);
        switch (statement) {
            case CLASS_DECLERATION:
                parseClassDecleration();
                break;
            case CLASS_VAR_DEC:
                parseClassVarDecleration();
                break;
            case SUBROUTINE_DEC:
                parseSubRoutineDeclaration();
                break;


            case VARIABLE_DECLARATION:
                parseVariableDeclaration();
                break;
            case CONDITIONAL:
                parseConditional();
                break;
            case ASSIGNMENT:
                parseAssignment();
                break;
            case RETURN:
                expect(SEMICOLON); end();
                break;
            case METHOD_CALLING:
                parseMethodCalling();
                break;
            case CLOSE_SCOPE:
                end();
                break;


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
        return statement;
    }

    /* This function init's the parser with the next line to parse. */
    private void initParser(String line) {
        this.tokensIterator = new Tokenizer(requireNonNull(line));
        currToken = getNextToken();
        nextToken = getNextToken();
    }

    /* This method in is charge of parsing the method declaration. */
    private void parseSubRoutineDeclaration() {
        MethodSymbol methodSymbol = new MethodSymbol(new VariableSymbol(VOID));
        if (nextTokenIs(ID)) {
            String methodName = currToken.getValue();
            if (validateMethodName(methodName)) {
                expect(L_PAREN);
                if (!nextTokenIs(R_PAREN)) {
                    do {
                        matchParameter(methodSymbol);
                    } while (nextTokenIs(COMMA));
                    expect(R_PAREN);
                }
                expect(L_BRACE);
                addMethodToScope(methodName, methodSymbol);
                end();
            }
        }
    }
    
    /* This method is in charge of validating the method's name. */
    private boolean validateMethodName(String methodName) {
        try {
            Tokenizer.validateMethodName(methodName);
            return true;
        } catch (UnknownTokenException e) {
            return false;
        }
    }

    /* This method is in charge of matching the parameters in the method declaration and add them to the
     method's symbol table.*/
    private void matchParameter(MethodSymbol methodSymbol) throws ParserException {
        VariableSymbolBuilder varBuilder = new VariableSymbolBuilder().makeInitialized();
        if (nextTokenIs(FINAL)) {
            varBuilder.makeFinal();
        }
        matchVariableType(varBuilder);
        expect(ID);
        addParameterToMethod(methodSymbol, currToken.getValue(), varBuilder.build());
    }

    /* This method adds the parameter the the given method table. */
    private void addParameterToMethod(MethodSymbol methodSymbol, String varName, VariableSymbol varSymbol)
            throws InvalidMethodParameterDefinitionException {
        try {
            methodSymbol.addParameter(varName, varSymbol);
        } catch (SymbolAlreadyExistsException e){
            throw new InvalidMethodParameterDefinitionException();
        }
    }

    /* This method matches the variable to it's type. */
    private void matchVariableType(VariableSymbolBuilder varBuilder){
        for (TokenType varType : SemanticAnalyzer.variableTypes) {
            if (nextTokenIs(varType)) {
                varBuilder.setType(varType);
                return;
            }
        }
        throw new InvalidMethodParameterDefinitionException();
    }

    /* This method is in charge of adding the parsed method from the declaration to the scope. */
    private void addMethodToScope(String methodName, MethodSymbol methodSymbol) throws MethodAlreadyDeclaredException {
        try {
            scope.addMethod(methodName, methodSymbol);
        }catch (SymbolAlreadyExistsException e) {
            throw new MethodAlreadyDeclaredException();
        }
    }

    /* This method is in charge of parsing the method calling. */
    private void parseMethodCalling() throws ParserException {
        String methodName = currToken.getValue();
        expect(L_PAREN);
        matchParameters(methodName);
        expect(R_PAREN);
        expect(SEMICOLON);
        end();
    }

    /* This method is in charge of matching the parameters given in the method calling to the method's
    actual parameters needed. */
    private void matchParameters(String methodName) throws ParserException {
        try {
            MethodSymbol methodSymbol = scope.lookupMethod(methodName);
            Iterator<Map.Entry<String, VariableSymbol>> params = methodSymbol.iterator();
            while (params.hasNext()) {
                VariableSymbol param = params.next().getValue();
                matchAssignmentTokenType(param.getType());
                verifyUsageIfNecessary(param.getType());
                if (params.hasNext()) {
                    expect(COMMA);
                }
            }
        } catch (NoSuchSymbolException | SemanticAnalyzerException e){
            throw new ParserException(e.getMessage());
        }
    }

    /* This method is in charge of parsing the assignment of new values to an existing parameter. */
    private void parseAssignment() throws ParserException {
        try {
            VariableSymbol varSymbol = semanticAnalyzer.getVariableIfDeclared(currToken.getValue());
            expect(EQUALS);
            matchExpression(varSymbol);
            expect(SEMICOLON);
            end();
        } catch (SemanticAnalyzerException e){
            throw new ParserException(e.getMessage());
        }
    }

    /* This method is in charge of matching assignment to the variable. */
    private void matchExpression(VariableSymbol varSymbol) throws ParserException, SemanticAnalyzerException {
        SemanticAnalyzer.requireNonFinal(varSymbol);
        matchAssignmentTokenType(varSymbol.getType());
        verifyAssignmentIfNecessary(varSymbol);
        varSymbol.toggle(Attribute.INITIALIZED);
    }

    /* This method is in charge of verifying the assignment types */
    private void verifyAssignmentIfNecessary(VariableSymbol varSymbol) throws SemanticAnalyzerException {
        if (currToken.getType() == ID) {
            semanticAnalyzer.verifyAssignment(varSymbol, currToken.getValue());
        }
    }

    /* This method is in charge of parsing the condition statement */
    private void parseConditional() throws ParserException {
        expect(L_PAREN);
        do {
            matchCondition();
        } while(nextTokenIs(AND) || nextTokenIs(OR));
        expect(R_PAREN);
        expect(L_BRACE);
        end();
    }

    /* This method checks if the condition presented is a valid one. */
    private void matchCondition() throws ParserException {
        try {
            matchAssignmentTokenType(BOOLEAN);
            verifyUsageIfNecessary(BOOLEAN);
        } catch (SemanticAnalyzerException e) {
            throw new ParserException(e.getMessage());
        }
    }

    /* This method is in charge of parsing the variable declaration */
    private void parseVariableDeclaration() throws ParserException {
        if (currToken.getType() == FINAL) {
            parseFinalVariableDeclaration(nextToken.getType());
        } else {
            parseNonFinalVariableDeclaration(currToken.getType());
        }
    }

    /* This method is in charge of parsing a declaration of a final variable. */
    private void parseFinalVariableDeclaration(TokenType varType) throws ParserException {
        expect(varType);
        do {
            expect(ID);
            matchAssignment(varType);
        } while (nextTokenIs(COMMA));
        expect(SEMICOLON);
        end();
    }

    /* This method is in charge of matching between assignments by type.  */
    private void matchAssignment(TokenType varType) throws ParserException {
        String varName = currToken.getValue();
        expect(EQUALS);
        addToScopeIfSemanticallyCorrect(varName,
                new VariableSymbolBuilder(varType).makeFinal().makeInitialized().build());
    }

    /* This method is in charge of parsing a non final variable. */
    private void parseNonFinalVariableDeclaration(TokenType varType) throws ParserException {
        do {
            VariableSymbolBuilder varBuilder = new VariableSymbolBuilder(varType);
            expect(ID);
            matchOptionalAssignment(currToken.getValue(), varBuilder);
        } while (nextTokenIs(COMMA));
        expect(SEMICOLON);
        end();
    }

    /* This method is in charge of matching the optional assignment (if exists) to the declared variable. */
    private void matchOptionalAssignment(String varName, VariableSymbolBuilder varBuilder) throws ParserException {
        if (nextTokenIs(EQUALS)) {
            varBuilder.makeInitialized();
            addToScopeIfSemanticallyCorrect(varName, varBuilder.build());
        } else {
            addVariableToScope(varName, varBuilder.build());
        }
    }
    /* This method is in charge of checking if the variable should be added to the scope */
    private void addToScopeIfSemanticallyCorrect(String varName, VariableSymbol varSymbol) throws ParserException {
        try {
            matchAssignmentTokenType(varSymbol.getType());
            verifyUsageIfNecessary(varSymbol.getType());
            addVariableToScope(varName, varSymbol);
        } catch (SemanticAnalyzerException e) {
            throw new ParserException(e.getMessage());
        }
    }

    private void matchAssignmentTokenType(TokenType varType) throws InvalidTokenForAssignment, ParserException {
        expect(semanticAnalyzer.verifyAssignmentTokenType(varType, nextToken.getType()));
    }

    /* This method is in charge of adding the variable to the scope */
    private void addVariableToScope(String varName, VariableSymbol varSymbol) throws VariableAlreadyDeclaredException {
        try {
            scope.addVariable(varName, varSymbol);
        } catch (SymbolAlreadyExistsException e) {
            throw new VariableAlreadyDeclaredException();
        }
    }

    /* This method is in charge of verifying the usage of the ID */
    private void verifyUsageIfNecessary(TokenType varType) throws SemanticAnalyzerException {
        if (currToken.getType() == ID) {
            semanticAnalyzer.verifyVariableUsage(varType, currToken.getValue());
        }
    }

    /* Assert that the statement should terminate, i.e. no more tokens are expected. */
    private void end() throws ParserException{
        if (nextToken != null) {
            throw new UnexpectedTokenTypeException(null);
        }
    }

    /* This method calls the next token and checks it's type */
    private boolean nextTokenIs(TokenType type) throws ParserException {
        if (nextToken != null && nextToken.getType() == type) {
            currToken = nextToken;
            nextToken = getNextToken();
            return true;
        }
        return false;
    }
    /* This method is in charge of checking the current token's type */
    private void expect(TokenType expected) throws ParserException {
        if (!nextTokenIs(expected)) {
            throw new UnexpectedTokenTypeException(expected);
        }
    }
    /* This method is in charge of getting the next token. */
    private Token getNextToken() throws ParserException {
        if (tokensIterator.hasNext()){
            try {
                return tokensIterator.next();
            } catch (UnknownTokenException e) {
                throw new ParserException(e.getMessage());
            }
        }
        return null;
    }
}
