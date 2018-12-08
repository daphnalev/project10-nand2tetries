package main.Scope;

import java.util.Map;

/**
 * Define a symbol table construct used to store identifiers (as keys) and their corresponding values (which may vary).
 * Identifiers represented as String object.
 * @param <E> Type of symbol table values.
 */
public interface SymbolTable<E> extends Iterable<Map.Entry<String, E>> {

    /**
     * Add entry to the symbol table.
     * @param identifier name of identifier.
     * @param value symbol corresponds to that identifier.
     * @throws SymbolAlreadyExistsException if symbol already exists in the table.
     */
    void add(String identifier, E value) throws SymbolAlreadyExistsException;

    /**
     * Get symbol of identifier.
     * @param identifier name of identifier.
     * @return the symbol stored in the table matching the identifier name.
     * @throws NoSuchSymbolException If identifier doesn't exists in the table.
     */
    E get(String identifier) throws NoSuchSymbolException;

    /**
     * Determine whether symbol table contains a mapping for the specified identifier.
     * @param identifier name of identifier.
     * @return true if and only if this symbol table contains a mapping for the specified identifier.
     */
    boolean contains(String identifier);
}
