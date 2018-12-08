package main.Scope;

import main.Lexer.TokenType;

/**
 * This class represents the variable symbol. It holds all the attributes a variable can be and it's type.
 */
public class VariableSymbol {

    /**
     * This enum declares all the available attributes of the variable.
     */
    public enum Attribute {
        FINAL(0x01),
        INITIALIZED(0x02);

        private int bitPosition;

        /**
         * Constructor.
         * @param bitPosition the bitPosition matching the enum.
         */
        Attribute (int bitPosition) {
            this.bitPosition = bitPosition;
        }
    }

    private TokenType type;
    private byte attributes;

    /**
     * Constructs new VariableSymbol object with given type.
     * @param symbolType the type of the attribute.
     */
    public VariableSymbol(TokenType symbolType) {
        this.type = symbolType;
        this.attributes = 0;
    }

    /**
     * Copy constructor.
     * @param variableSymbol a variable symbol to copy.
     */
    public VariableSymbol(VariableSymbol variableSymbol) {
        this.type = variableSymbol.type;
        this.attributes = variableSymbol.attributes;
    }

    /**
     * This methods sets the type of the variable.
     * @param type the type of the token.
     */
    public void setType(TokenType type) {
        this.type = type;
    }

    /**
     * @return the type of the variable.
     */
    public TokenType getType() {
        return type;
    }

    /**
     * This methods checks if the given attributes are activated in the variable.
     * @param attributes varArgs of attributes
     * @return true if all the attributes given are activated, false otherwise.
     */
    public boolean isActivated(Attribute... attributes) {
        byte mask = getMask(attributes);
        return (this.attributes & mask) != 0;
    }

    /**
     * This method toggles the given attributes.
     * @param attribute varArgs of attributes
     */
    public void toggle(Attribute attribute) {
        byte mask = getMask(attribute);
        this.attributes ^= mask;
    }

    /* This method returns the attribute bit mask */
    private byte getMask(Attribute... attributes) {
        byte mask = 0;
        for (Attribute attribute : attributes){
            mask |= attribute.bitPosition;
        }
        return mask;
    }
}
