package main;
import main.Interpreter.Parser;
import main.Lexer.TokenType;
import main.Scope.Scope;
import main.Scope.VariableSymbolBuilder;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static main.Lexer.TokenType.*;
import static main.Scope.VariableSymbol.Attribute;

public class AssignmentParserTest {
    private Scope scope;

    // Builds a generic scope with these properties:
    // #1 - uninitialized and non-final, #2 - initialized and non-final,
    // #1 - uninitialized and non-final, #2 - initialized and non-final,
    // #3 - uninitialized and final, #4 - initialized and final.
    @BeforeEach
    void createScope() throws SymbolAlreadyExistsException {
        scope = new Scope();
        TokenType[] varTypes = new TokenType[] { INT, BOOLEAN, DOUBLE, CHAR, STRING };
        for (TokenType varType : varTypes) {
            char firstLetter = varType.toString().toLowerCase().charAt(0);
            scope.addVariable(firstLetter + "1", new VariableSymbolBuilder(varType).build());
            scope.addVariable(firstLetter + "2", new VariableSymbolBuilder(varType).makeInitialized().build());
            scope.addVariable(firstLetter + "3", new VariableSymbolBuilder(varType).makeFinal().build());
            scope.addVariable(firstLetter + "4", new VariableSymbolBuilder(varType).makeInitialized().makeFinal().build());
        }
    }

    @Test
    void testFinalAssignment() {
        shouldFail("i3", "i2");
        shouldFail("i4", "i2");
    }

    @Test
    void testUninitializedAssignment() {
        shouldSuccess("i1", "7");
        assertInitialized("i1");
    }

    @Test
    void testContravariantTypeAssignment() {
        shouldFail("i1", "7.2");
        shouldFail("c1", "5");
        shouldFail("s1", "'a'");
        shouldFail("i1", "s4");
        shouldFail("i1", "c2");
    }

    @Test
    void testAssignmentOfUninitializedVariable() {
        shouldFail("i1", "i3");
        shouldFail("i1", "i1");
    }

    @Test
    void testCovariantTypeAssignment() {
        shouldSuccess("i1", "i2");
        shouldSuccess("i1", "i4");
        shouldSuccess("b1", "b2");
        shouldSuccess("b1", "i2");
        shouldSuccess("b1", "d2");
    }

    @Test
    void testDeclaringSameID() {
        shouldFail("int a = 5, a=7;");
    }

    @Test
    void testDeclaringUnorderedID() {
        shouldFail("int a=b;");
        shouldSuccess("int b=2;");
    }

    @Test
    void testDeclaringUnorderedIDFinal() {
        shouldFail("final int a=b;");
    }

    @Test
    void UninitializedFinal() {
        shouldFail("final int a;");
    }

    @Test
    void ReassignFinalParameter() {
        Parser parser = new Parser(scope);
        try {
            parser.parse("final int a=5;");
            parser.parse("a=10;");
        } catch (ParserException e) {

        } catch (Exception e){
            fail("Unexpected exception thrown: " + e.getMessage());
        }
    }

    private void shouldSuccess(String assignTo, String assigned) {
        Parser parser = new Parser(scope);
        try {
            parser.parse(assignTo + "=" + assigned + ";");
        } catch (Exception e) {
            fail("Unexpected exception thrown: " + e.getMessage());
        }
    }

    private void shouldSuccess(String line) {
        Parser parser = new Parser(scope);
        try {
            parser.parse(line);
        } catch (Exception e) {
            fail("Unexpected exception thrown: " + e.getMessage());
        }
    }

    private void shouldFail(String assignTo, String assigned) {
        Parser parser = new Parser(scope);
        assertThrows(ParserException.class, () -> parser.parse(assignTo + "=" + assigned + ";"));
    }

    private void shouldFail(String line) {
        Parser parser = new Parser(scope);
        assertThrows(ParserException.class, () -> parser.parse(line));
    }

    private void assertInitialized(String varName) {
        try {
            assertTrue(scope.lookupVariable(varName).isActivated(Attribute.INITIALIZED));
        }catch (NoSuchSymbolException e) {
            fail("Expected symbol to be in the scope.");
        }
    }
}
