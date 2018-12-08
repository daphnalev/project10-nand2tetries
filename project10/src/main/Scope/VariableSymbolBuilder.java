package main.Scope;
import main.Lexer.TokenType;
import main.Scope.VariableSymbol.Attribute;

/**
 * Builds new variableSymbol object which is indented to be contained in the scope variableSymbol table.
 * This class implements the Builder design-pattern to aid the construction of the VariableSymbol complex object,
 * and abstract it's implementation.
 */
public class VariableSymbolBuilder {
    private VariableSymbol variableSymbol;

    /**
     * Constructs new VariableSymbolBuilder with VOID type.
     */
    public VariableSymbolBuilder() {
        this.variableSymbol = new VariableSymbol(TokenType.VOID);
    }

    /**
     * Constructs new VariableSymbolBuilder with given type.
     * @param type of the variable
     */
    public VariableSymbolBuilder(TokenType type) {
        this.variableSymbol = new VariableSymbol(type);
    }

    /**
     * Update the type held.
     * @param type of the token needed.
     * @return this VariableSymbolBuilder
     */
    public VariableSymbolBuilder setType(TokenType type) {
        this.variableSymbol.setType(type);
        return this;
    }

    /**
     * Activates the final attribute of the variable.
     * @return this VariableSymbolBuilder
     */
    public VariableSymbolBuilder makeFinal() {
        this.variableSymbol.toggle(Attribute.FINAL);
        return this;
    }

    /**
     * Activates the initialized attribute of the variable.
     * @return this VariableSymbolBuilder
     */
    public VariableSymbolBuilder makeInitialized() {
        this.variableSymbol.toggle(Attribute.INITIALIZED);
        return this;
    }

    /**
     * This function builds the VariableSymbol with the specified characteristics.
     * @return the variableSymbol created.
     */
    public VariableSymbol build() {
        return this.variableSymbol;
    }
}
