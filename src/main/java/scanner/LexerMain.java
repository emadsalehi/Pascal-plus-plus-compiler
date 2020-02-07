package scanner;

import java_cup.runtime.Symbol;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

public class LexerMain {

    private static final Logger LOGGER = Logger.getLogger(LexerMain.class.getName());

    public static void exec(String fileName) {
        FileReader srcFile = null;
        String srcName =  fileName + ".pascal";
        try {
            srcFile = new FileReader(srcName);
        } catch (FileNotFoundException e) {
            LOGGER.info("Code file cannot be opened!");
            e.printStackTrace();
        }
        PascalLexer pascalLexer = new PascalLexer(srcFile);
        Symbol symbol;
        try {
            while ((symbol = pascalLexer.next_token()).sym != sym.EOF) {

            }
        } catch (IOException e) {

        }
    }
}
