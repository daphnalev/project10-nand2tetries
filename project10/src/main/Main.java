package main;

import java.io.*;
import main.Interpreter.*;

/**
 * Main program driver; receive an jack file and interprets it according to the language specification.
 */
public class Main {


    private static final int NUMBER_OF_ARGUMENTS = 1;
    /**
     * Received an Jack program to interpret according to Jack language specification.
     * @param args expects one argument, the path to the Jackfile (absolute or relative).
     */
    public static void main(String[] args) {
        // todo: check if argument is path and run over all files accordingly

        String file = args[0];

        Interpreter interpreter = new Interpreter(file);
        interpreter.interpret();
    }
}
