package main.Scope;

import java.util.Map;

/**
 * This class represents the scope object, this object holds all the variables and methods declared in the scope.
 * This class support scope chaining used to comply with the sJava language specification.
 */
public class Scope {
    private SymbolTable<VariableSymbol> variablesTable;
    private SymbolTable<MethodSymbol> methodsTable;
    private Scope prev;

    /**
     * Construct new scope with empty symbol tables.
     */
    public Scope() {
        this.variablesTable = new SymbolTableImpl<>();
        this.methodsTable = new SymbolTableImpl<>();
        this.prev = null;
    }

    /**
     * Construct new scope which is nested in given scope.
     * @param prev enclosing scope.
     */
    public Scope(Scope prev) {
        this();
        this.prev = prev;
    }

    /**
     * Add variable to the current scope.
     * @param identifier of the symbol to add to the scope
     * @param symbol the symbol of variable
     * @throws SymbolAlreadyExistsException when the variable already exists in the symbolTable.
     */
    public void addVariable(String identifier, VariableSymbol symbol) throws SymbolAlreadyExistsException {
        this.variablesTable.add(identifier, symbol);
    }

    /**
     * Add method to the current scope.
     * @param methodName the method to add
     * @param symbol the symbol to add
     * @throws SymbolAlreadyExistsException when the symbol already exists in the symbolTable.
     */
    public void addMethod(String methodName, MethodSymbol symbol) throws SymbolAlreadyExistsException {
        this.methodsTable.add(methodName, symbol);
    }

    /**
     * Perform a lookup for given identifier in the current scope and any scope enclosing it.
     * @param identifier name of the symbol needed
     * @return the variableSymbol of the identifier.
     * @throws NoSuchSymbolException when no symbols with the given ID were found.
     */
    public VariableSymbol lookupVariable(String identifier) throws NoSuchSymbolException {
        for (Scope scope = this; scope != null; scope = scope.prev) {
            if (scope.variablesTable.contains(identifier)){
                return scope.variablesTable.get(identifier);
            }
        }
        throw new NoSuchSymbolException();
    }

    /**
     * Perform a lookup for given method in the current scope and any scope enclosing it.
     * @param methodName the name of the method to find
     * @return the methodSymbol of the method searched.
     * @throws NoSuchSymbolException  when no methods with the given ID were found.
     */
    public MethodSymbol lookupMethod(String methodName) throws NoSuchSymbolException {
        for (Scope scope = this; scope != null; scope = scope.prev) {
            if (scope.methodsTable.contains(methodName)){
                return scope.methodsTable.get(methodName);
            }
        }
        throw new NoSuchSymbolException();
    }

    /**
     * Perform a lookup for given method in the current scope and any scope enclosing it.
     * @param methodOrdinal the order of the method in the sjava file.
     * @return the methodSymbol of the method searched.
     * @throws NoSuchSymbolException  when no methods with the given ID were found.
     */
    public MethodSymbol lookupMethod(int methodOrdinal) throws NoSuchSymbolException {
        int i = 0;
        for(Map.Entry<String, MethodSymbol> entry : methodsTable) {
            if (i == methodOrdinal) {
                return entry.getValue();
            }
            i++;
        }
        throw new NoSuchSymbolException();
    }

    /**
     * @return return the previous scope.
     */
    public Scope getPrev() {
        return prev;
    }

    /**
     * Duplicate the current scope, deep copying the Scopes attributes.
     * @return a new duplicated scope from the original one.
     */
    public Scope duplicate()  {
        Scope scope = new Scope();
        try {
            for (Map.Entry<String, VariableSymbol> entry : variablesTable) {
                scope.variablesTable.add(entry.getKey(), new VariableSymbol(entry.getValue()));
            }
            scope.methodsTable = this.methodsTable;
        } catch (SymbolAlreadyExistsException e) {
            return scope;
        }
        return scope;
    }
}
