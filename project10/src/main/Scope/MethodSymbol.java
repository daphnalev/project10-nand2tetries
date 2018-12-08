package main.Scope;

import java.util.Iterator;
import java.util.Map;

/**
 * This class represents a Method, and holds all the attributes that are specific to the method.
 * The methods parameters:
 * - Method's return type
 * - Method's parameters (can be iterated through).
 * @author Moshe Kol, Yael Sarusi
 */

public class MethodSymbol implements Iterable<Map.Entry<String, VariableSymbol>> {
    private VariableSymbol returnType;
    private SymbolTable<VariableSymbol> parameters;

    /**
     * Constructor.
     * @param returnType the return type expected from the method.
     */
    public MethodSymbol(VariableSymbol returnType) {
        this.returnType = returnType;
        parameters = new SymbolTableImpl<>();
    }

    /**
     * This function adds a symbol to the symbol table of the method.
     * @param paramName the new parameter name
     * @param paramSymbol the data type that holds the parameter data.
     */
    public void addParameter(String paramName, VariableSymbol paramSymbol){
        parameters.add(paramName, paramSymbol);
    }

    /**
     * @return the Methods' return type.
     */
    public VariableSymbol getReturnType() {
        return returnType;
    }

    /**
     * @return Iterator that iterates through the methods parameter.
     */
    @Override
    public Iterator<Map.Entry<String, VariableSymbol>> iterator() {
        return parameters.iterator();
    }
}
