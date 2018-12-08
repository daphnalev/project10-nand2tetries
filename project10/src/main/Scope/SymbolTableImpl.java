package main.Scope;
import java.util.*;
import static java.util.Objects.*;

/**
 * Symbol table implementation generic type Class.
 * @param <E> generic type.
 */
public class SymbolTableImpl<E> implements SymbolTable<E> {

    private SortedMap<String, E> table;

    SymbolTableImpl() {
        this.table = new TreeMap<>();
    }

    @Override
    public void add(String identifier, E value) throws SymbolAlreadyExistsException {
        if (contains(identifier)) {
            throw new SymbolAlreadyExistsException();
        }
        table.put(identifier, requireNonNull(value));
    }

    @Override
    public E get(String identifier) throws NoSuchSymbolException {
        if (contains(identifier)) {
            return table.get(identifier);
        }
        throw new NoSuchSymbolException();
    }

    @Override
    public boolean contains(String identifier) {
        return table.containsKey(requireNonNull(identifier));
    }

    @Override
    public Iterator<Map.Entry<String, E>> iterator() {
        return table.entrySet().iterator();
    }
}
