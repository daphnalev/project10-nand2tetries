package main.Interpreter;

import main.Lexer.Token;
import main.Lexer.TokenType;

/**
 * This Class is the Statement factory. It decides which type of statement is parsed in the current line.
 */

class StatementFactory {
    /**
     * This static function receives the first 2 tokens that represents the line currently parsed, and
     * returns the StatementType of the
     * @param firstToken The first token of the line
     * @param secondToken The second token of the line
     * @return The Statement that matches the line
     * statement types.
     */
    static StatementType getStatement(Token firstToken, Token secondToken) {
        switch (firstToken.getType()) {
            case INT:
            case BOOLEAN:
            case CHAR:
            case ID: //todo: not sure, object type class
                return StatementType.VARIABLE_DECLARATION;
            case RETURN:
                return StatementType.RETURN;
            case WHILE:
            case IF:
                return StatementType.CONDITIONAL;
            case VOID:
                return StatementType.METHOD_DECLARATION;
            case ID:
                if (secondToken != null) {
                    if (secondToken.getType() == TokenType.EQUALS) {
                        return StatementType.ASSIGNMENT;
                    } else if (secondToken.getType() == TokenType.L_PAREN) {
                        return StatementType.METHOD_CALLING;
                    }
                }
            case R_BRACE:
                return StatementType.CLOSE_SCOPE;
        }
        throw new UnknownStatementException();
    }
}
