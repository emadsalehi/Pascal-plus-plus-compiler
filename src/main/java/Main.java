import parser.Parser;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Pascal++ Compiler");
        Scanner input = new Scanner(System.in);
        String programName = input.next();
        Parser parser = new Parser();
        parser.parse(programName);
    }
}
