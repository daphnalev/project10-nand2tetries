package main;

import main.Interpreter.*;
import main.Lexer.TokenType;
import main.Scope.Scope;
import main.Scope.VariableSymbolBuilder;
import org.junit.jupiter.api.*;

import static main.Lexer.TokenType.*;
import static org.junit.jupiter.api.Assertions.*;

class ConditionParserTest {
    private static Scope scope = new Scope();

    // Builds a generic scope with these properties:
    // #1 - uninitialized and non-final, #2 - initialized and non-final,
    // #3 - uninitialized and final, #4 - initialized and final.
    static {
        try {
            TokenType[] varTypes = new TokenType[] { INT, BOOLEAN, DOUBLE, CHAR, STRING };
            for (TokenType varType : varTypes) {
                char firstLetter = varType.toString().toLowerCase().charAt(0);
                scope.addVariable(firstLetter + "1", new VariableSymbolBuilder(varType).build());
                scope.addVariable(firstLetter + "2", new VariableSymbolBuilder(varType).makeInitialized().build());
                scope.addVariable(firstLetter + "3", new VariableSymbolBuilder(varType).makeFinal().build());
                scope.addVariable(firstLetter + "4", new VariableSymbolBuilder(varType).makeInitialized().makeFinal().build());
            }
        } catch (Exception e){

        }
    }

    @Test
    void testCorrectConditions() throws Exception {
        for (TokenType conditionType : new TokenType[] { IF, WHILE }) {
            testCorrectCondition(conditionType, "true");
            testCorrectCondition(conditionType, "false");
            testCorrectCondition(conditionType, "-7");
            testCorrectCondition(conditionType, "7.2");
            testCorrectCondition(conditionType, "7 && 6");
            testCorrectCondition(conditionType, "7 || 6");
            testCorrectCondition(conditionType, "b2 && b4 && i2  ");
        }
    }

    @Test
    void testIncorrectConditions() {
        for (TokenType conditionType : new TokenType[] { IF, WHILE }) {
            testIncorrectCondition(conditionType, "'a'", InvalidTokenForAssignment.class);
            testIncorrectCondition(conditionType, "\"aaasgag\"", InvalidTokenForAssignment.class);
            testIncorrectCondition(conditionType, "b1", UninitializedVariableUsageException.class);
            testIncorrectCondition(conditionType, "b3", UninitializedVariableUsageException.class);
            testIncorrectCondition(conditionType, "b5", UndeclaredVariableUsageException.class);
            testIncorrectCondition(conditionType, "b2 || i2 && b1", UninitializedVariableUsageException.class);
            testIncorrectCondition(conditionType, "b2 || i2 && ", InvalidTokenForAssignment.class);
        }
    }

    private void testCorrectCondition(TokenType conditionType, String conditions) throws Exception {
        Parser parser = new Parser(scope);
        parser.parse(conditionType.toString().toLowerCase() + "(" + conditions + "){");
    }

    private void testIncorrectCondition(TokenType conditionType, String conditions, Class<? extends Throwable> exceptionClass) {
        Parser parser = new Parser(scope);
        try {
            parser.parse(conditionType.toString().toLowerCase() + "(" + conditions + "){");
        } catch (ParserException e) {
            if (e.getCause() != null) {
                assertEquals(exceptionClass, e.getCause().getClass());
            }
        } catch (Exception e) {
            fail("");
        }

    }
}
