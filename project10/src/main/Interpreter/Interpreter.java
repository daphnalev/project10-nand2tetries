package main.Interpreter;

import main.Lexer.Tokenizer;
import main.Scope.*;
import static main.Interpreter.StatementType.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Carries out the process of interpreting sJavac file.
 * This class encapsulate all classes present on this module.
 */
public class Interpreter {

    /* Helper class that keeps track of scope opening and closing. */
    private static class ScopeHandler {
        /**
         * Defines different modes on which the scope handler should operate.
         */
        public enum ScopeHandlerMode {
            MAINTAIN_ONLY, // Maintains scope depth, without instantiating new scope objects.
            MAINTAIN_AND_SWITCH // In addition to maintaining scope depth, instantiating new scope objects when needed.
        }

        private static final String OPENING_SCOPE_REGEX = ".*?\\{\\s*";
        private static final String CLOSING_SCOPE_REGEX = "\\s*}\\s*";

        private Scope scope;
        private Scope lastScope;
        private ScopeHandlerMode mode;
        private int depth;
        private int lastDepth;

        /**
         * Initialize new scope handler object with given scope (regarded as global scope), and given mode of operation.
         * @param globalScope non-null scope object to be initialized.
         * @param mode the mode of operation.
         */
        ScopeHandler(Scope globalScope, ScopeHandlerMode mode) {
            this.scope = globalScope;
            this.lastScope = null;
            this.depth = 0;
            this.lastDepth = 0;
            this.mode = mode;
        }

        /**
         * @return current scope object held in the ScopeHandler.
         */
        public Scope getScope() {
            return scope;
        }

        /**
         * @return true if and only if current scope held is global.
         */
        boolean isGlobalScope() {
            return depth == 0;
        }

        /**
         * Maintain scope depth based on given line.
         * @param line line to process.
         * @throws MismatchBracesException if number of closing braces is greater than the number of opening braces.
         */
        void accept(String line) throws MismatchBracesException {
            if (line == null) { return; }
            if (shouldOpenScope(line)) {
                lastDepth = depth++;
                switchIfNeeded();
            } else if (shouldCloseScope(line)) {
                lastDepth = depth--;
                if (depth < 0) { throw new MismatchBracesException(); }
                switchIfNeeded();
            }
        }

        /* Determine whether scope should open. */
        private boolean shouldOpenScope(String line) {
            return !Tokenizer.isComment(line) && line.matches(OPENING_SCOPE_REGEX);
        }

        /* Determine whether scope should close. */
        private boolean shouldCloseScope(String line) {
            return !Tokenizer.isComment(line) && line.matches(CLOSING_SCOPE_REGEX);
        }

        /* Switch scopes if the current scope handler mode requires it. */
        private void switchIfNeeded() {
            if (mode == ScopeHandlerMode.MAINTAIN_AND_SWITCH) {
                if (depth > lastDepth) {
                    openScope();
                } else if (depth < lastDepth) {
                    closeScope();
                }
            }
        }

        /* Append new scope to the current scope. */
        private void openScope() {
            lastScope = scope;
            scope = new Scope(lastScope);
        }

        /* Unwind the scope - effectively removes the last scope from the chain. */
        private void closeScope() {
            scope = lastScope;
            lastScope = scope.getPrev();
        }
    }

    private final Path filepath;
    private List<Integer> methodDeclarationLines;
    private Scope global;

    /**
     * Initialize new interpreter class with given filepath.
     * @param filepath non-null string represents a filepath (absolute or relative).
     */
    public Interpreter(String filepath) {
        this.filepath = Paths.get(filepath);
        this.global = new Scope();
        methodDeclarationLines = new ArrayList<>();
    }

    /**
     * Interpret the file. This is two stage interpretation. At first, the file is briefly analyzed to populate the
     * global scope table. The second stage is responsible for interpreting every line.
     */
    public void interpret() throws IOException {
        firstPass(); // not sure first pass is needed - grammar is already syntactically correct
        secondPass();
    }

    /* This function carries out the first stage of the interpretation process.
     * At this stage, the file is briefly analyzed, restricted to the global scope only.
     * Global variable declarations are parsed, as well as method declarations. */
    private void firstPass() throws IOException {
        try (LineNumberReader reader = new LineNumberReader(Files.newBufferedReader(filepath))) {
            doFirstPass(reader);
        }
    }

    /* Executes the first pass stage. */
    private void doFirstPass(LineNumberReader reader) throws IOException {
        ScopeHandler scopeHandler = new ScopeHandler(global, ScopeHandler.ScopeHandlerMode.MAINTAIN_ONLY);
        Parser globalParser = new Parser(global);
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            preProcessLine(line, reader.getLineNumber(), scopeHandler, globalParser);

    }

    /* Pre-process lines if and only if there exists in the global scope. */
    private void preProcessLine(String line, int lineNumber, ScopeHandler scopeHandler, Parser globalParser)
            throws InterpreterException, MismatchBracesException, ParserException, UnknownStatementException {
        if (scopeHandler.isGlobalScope()) {
            StatementType statement = globalParser.parse(line);
            verifyGlobalStatement(statement, lineNumber);
        }
        scopeHandler.accept(line);
    }

    /* Verify that the given statement is allowed to be in the global scope. */
    private void verifyGlobalStatement(StatementType statement, int lineNumber){
        if (statement == null) { return; }
        SemanticAnalyzer.verifyGlobalStatement(statement);
        if (statement == METHOD_DECLARATION) {
            methodDeclarationLines.add(lineNumber); // This is later used by the second interpretation stage.
        }
    }

    /* Constitutes the second stage of interpretation.
     * At this stage, the file is analyzed more thoroughly, skipping lines previously analyzed for better performance.
     * Each "method" line is parsed using the Parser object. */
    private void secondPass() throws IOException, InterpreterException {
        try (LineNumberReader lineNumberReader = new LineNumberReader(Files.newBufferedReader(filepath))) {
            doSecondPass(lineNumberReader);
        }
    }

    /* Executes the second pass stage. */
    private void doSecondPass(LineNumberReader reader) throws IOException, InterpreterException {
        try {
            for (int methodOrdinal = 0; methodOrdinal < methodDeclarationLines.size(); methodOrdinal++) {
                ScopeHandler scopeHandler = new ScopeHandler(global.duplicate(),
                        ScopeHandler.ScopeHandlerMode.MAINTAIN_AND_SWITCH);
                skipUntil(reader, methodDeclarationLines.get(methodOrdinal));
                scopeHandler.accept(reader.readLine()); // As a consequence, open new scope
                initScopeWithMethodParameters(scopeHandler.getScope(), methodOrdinal, reader.getLineNumber());
                processMethod(reader, scopeHandler);
            }
        } catch (MismatchBracesException e) {
            throw new InterpreterException(reader.getLineNumber(), e.getMessage());
        }
    }

    /* Keep reading from reader until lineNumBound is reached. */
    private void skipUntil(LineNumberReader reader, int lineNumBound) throws IOException {
        for (int i = reader.getLineNumber() + 1; i < lineNumBound; i++){
            reader.readLine();
        }
    }

    /* Initialize given scope with the parameters of the method whose location inside the file is
     given by methodOrdinal.
     Assume that given scope is newly created, otherwise throws FailedMethodScopeInitializationException. */
    private void initScopeWithMethodParameters(Scope scope, int methodOrdinal, int lineNumber)
            throws FailedMethodScopeInitializationException {
        try {
            MethodSymbol methodSymbol = global.lookupMethod(methodOrdinal);
            for (Map.Entry<String, VariableSymbol> varEntry : methodSymbol) {
                scope.addVariable(varEntry.getKey(), varEntry.getValue());
            }
        } catch (NoSuchSymbolException | SymbolAlreadyExistsException e) {
            throw new FailedMethodScopeInitializationException(lineNumber);
        }
    }

    /* Reader object contains the first line of a the method block.
     * ScopeHandler is updated with the method parameters.
     * This method parses each method line, and validate it. */
    private void processMethod(LineNumberReader reader, ScopeHandler scopeHandler)
            throws IOException, InterpreterException, MismatchBracesException {
        try {
            StatementType statement = null, prevStatement = null;
            String line;
            while ((line = reader.readLine()) != null) {
                scopeHandler.accept(line);
                Parser parser = new Parser(scopeHandler.getScope());
                prevStatement = statement;
                statement = parser.parse(line);
                validateMethodStatement(statement, reader.getLineNumber());
                if (scopeHandler.isGlobalScope()) {
                    break;
                }
            }
            verifyCorrectMethodClosing(statement, prevStatement, reader.getLineNumber());
        } catch (ParserException | UnknownStatementException e) {
            throw new InterpreterException(reader.getLineNumber(), e.getMessage());
        }
    }

    /* Assert that a method block is ended with the correct pattern. */
    private void verifyCorrectMethodClosing(StatementType statement, StatementType prevStatement, int lineNumber)
            throws MissingReturnStatementException {
        if (!(prevStatement == RETURN && statement == CLOSE_SCOPE)) {
            throw new MissingReturnStatementException(lineNumber);
        }
    }

    /* Validate that the given method statement is valid. */
    private void validateMethodStatement(StatementType statement, int lineNumber) throws InterpreterException{
        if (statement == null) { return; }
        try {
            SemanticAnalyzer.verifyMethodStatement(statement);
        } catch (UnexpectedMethodStatementException e) {
            throw new InterpreterException(lineNumber, e.getMessage());
        }
    }
}
