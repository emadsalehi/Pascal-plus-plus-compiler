package parser;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java_cup.runtime.Symbol;
import scanner.PascalLexer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class Parser {

    private static final Logger LOGGER = Logger.getLogger(Parser.class.getName());
    private static final String SHIFT = "SHIFT";
    private static final String PUSH_GOTO = "PUSH_GOTO";
    private static final String REDUCE = "REDUCE";
    private static final String GOTO = "GOTO";
    private static final String ACCEPT = "ACCEPT";
    private static final String ERROR = "ERROR";

    private Map<String, Map<Integer, String>> parseTable = new HashMap<String, Map<Integer, String>>();
    private Stack<Integer> parseStack = new Stack<Integer>();
    private int currentState = 1;

    public void parse(String fileName) {
        CSVReader csvReader = null;
        List<String[]> parseTableEntries = null;
        try {
            FileReader parseTableFileReader = new FileReader("ParseTable.csv");
            csvReader = new CSVReaderBuilder(parseTableFileReader).withSkipLines(0).build();
            parseTableEntries = csvReader.readAll();
        } catch (FileNotFoundException e) {
            LOGGER.info("ParseTable file couldn't read");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] tokens = parseTableEntries.get(0);
        List<String> states = new ArrayList<String>();

        // Read states
        for (int i = 1 ; i < parseTableEntries.size() ; i++)
            states.add(parseTableEntries.get(i)[0]);
        // Construct tokens
        for (int i = 1 ; i <  tokens.length ; i++)
            parseTable.put(tokens[i], new HashMap<Integer, String>());
        // Create ParseTable
        for (String[] row : parseTableEntries) {
            for (int i = 1 ; i < row.length ; i++) {
                parseTable.get(tokens[i]).put(Integer.valueOf(row[0]), row[i]);
            }
        }

        FileReader codeFile = null;
        String srcName =  fileName + ".pascal";
        try {
            codeFile = new FileReader(srcName);
        } catch (FileNotFoundException e) {
            LOGGER.info("Code file cannot be opened!");
            e.printStackTrace();
        }

        PascalLexer pascalLexer = new PascalLexer(codeFile);

        try {
            Symbol symbol = pascalLexer.next_token();
            String token = String.valueOf(symbol.sym);
            boolean alive = true;
            while (alive) {
                String[] actionParams = parseTable.get(token).get(currentState).split(" ");
                String mainAction = actionParams[0];
                String codeGenToken = actionParams[actionParams.length - 1];
                if (mainAction.equals(SHIFT)) {
                    currentState = Integer.parseInt(actionParams[1].substring(1));
                    symbol = pascalLexer.next_token();
                    token = String.valueOf(symbol.sym);
                } else if (mainAction.equals(PUSH_GOTO)) {
                    parseStack.push(currentState);
                    currentState = Integer.parseInt(actionParams[1].substring(1));
                } else if (mainAction.equals(GOTO)) {
                    currentState = Integer.parseInt(actionParams[1].substring(1));
                } else if (mainAction.equals(REDUCE)) {
                    currentState = parseStack.pop();
                    token = actionParams[1];
                } else if (mainAction.equals(ERROR)) {
                    throw new Exception("Code cannot be compiled");
                } else if (mainAction.equals(ACCEPT)) {
                    System.out.println("Compiled");
                    alive = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
