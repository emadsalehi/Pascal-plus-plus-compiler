package code_generator;

import java_cup.runtime.Symbol;
import parser.IDescription;

import java.util.*;

public class CodeGenerator {

    private int stp;
    private int tempNumber = 0;
    private int tempCounter = 0;
    private Stack<String> semanticStack = new Stack<String>();
    private Map<String, IDescription> symbolTable = new HashMap<String, IDescription>();
    private List<String> codeArray = new ArrayList<String>();

    public void generateCode(String sem, Symbol symbol) throws Exception {
        int codeSize = codeArray.size();
        if (sem.equals("pushNew")) {
            semanticStack.push((String) symbol.value);
        } else if (sem.equals("pushType")) {
            semanticStack.push((String) symbol.value);
        } else if (sem.equals("vdscp") || sem.equals("adscp")) {
            int typeSymbol = Integer.parseInt(semanticStack.pop());
            String id = semanticStack.peek();
            String variableType = "";
            switch (typeSymbol) {
                case 4:
                    variableType = "i1";
                    break;
                case 5:
                    variableType = "i8";
                    break;
                case 6:
                    variableType = "i32";
                    break;
                case 7:
                    variableType = "float";
                    break;
                case 8:
                    variableType = "i8*";
                    break;
            }
            if (sem.equals("vdscp")) {
                symbolTable.put(id, new IDescription(variableType, false));
                String code = codeSize + ":\n %" + id + " = alloca " + variableType;
                codeArray.add(code);
//                codeSize++;
//                codeArray.add(codeSize + ":\n %" + id + " = load " + variableType + ", " + variableType + "* %ptr" + id);
            }
            else {
                StringBuilder arrayVariableType = new StringBuilder(variableType);
                Stack<Integer> sizeList = new Stack<Integer>();
                for (int i = 0 ; i < tempCounter ; i++) {
                    int dimension = Integer.parseInt(semanticStack.pop());
                    arrayVariableType = new StringBuilder("[" + dimension + " x " + arrayVariableType + "]");
                    sizeList.add(Integer.parseInt(semanticStack.pop()));
                }
                symbolTable.put(id, new IDescription(variableType, true));
                IDescription iDescription = new IDescription(arrayVariableType.toString(), true);
                // TODO : Add dimension description  to iDescription
                symbolTable.put(id, iDescription);
                String code = codeSize + ":\n %ptr" + id + " = alloca " + arrayVariableType;
                codeArray.add(code);
            }
        } else if (sem.equals("asgnDcl")) {
            String assignId = semanticStack.pop();
            String id = semanticStack.peek();
            String assignVariableType = symbolTable.get(assignId).getType();
            String variableType = symbolTable.get(semanticStack.peek()).getType();
            codeSize++;
            if (!assignCheckType(assignVariableType, assignId, variableType)) throw new Exception("Wrong assign variable check type");
            codeArray.add(codeSize + ":\n store " + variableType + " %" + assignId + ", " + variableType + "* %" + id);
        } else if (sem.equals("clearCnt")) {
            tempCounter = 0;
        } else if (sem.equals("pushNumber")) {
            semanticStack.push(((Integer) symbol.value).toString());
            tempCounter++;
        } else if (sem.equals("pop")) {
            semanticStack.pop();
        } else if (sem.equals("mul")) {
            String id1 = semanticStack.pop();
            String id2 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = operationCheckType(variableType1, id1, variableType2, id2, "mul");
            if (referenceVariableType.equals("float")) {
                codeArray.add(codeSize + ":\n %" + tempNumber + " = fmul " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add(codeSize + ":\n %" + tempNumber + " = mul " + referenceVariableType + " %" + id1 + " ,%" + id2);
            }
            semanticStack.push(String.valueOf(tempNumber));
            symbolTable.put(String.valueOf(tempNumber), new IDescription(referenceVariableType, false));
            tempNumber++;
        } else if (sem.equals("div")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = operationCheckType(variableType1, id1, variableType2, id2, "mul");
            if (referenceVariableType.equals("float")) {
                codeArray.add(codeSize + ":\n %" + tempNumber + " = fdiv " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add(codeSize + ":\n %" + tempNumber + " = div " + referenceVariableType + " %" + id1 + " ,%" + id2);
            }
            semanticStack.push(String.valueOf(tempNumber));
            symbolTable.put(String.valueOf(tempNumber), new IDescription(referenceVariableType, false));
            tempNumber++;
        } else if (sem.equals("mod")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = operationCheckType(variableType1, id1, variableType2, id2, "mul");
            if (referenceVariableType.equals("float")) {
                codeArray.add(codeSize + ":\n %" + tempNumber + " = frem " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add(codeSize + ":\n %" + tempNumber + " = srem " + referenceVariableType + " %" + id1 + " ,%" + id2);
            }
            semanticStack.push(String.valueOf(tempNumber));
            symbolTable.put(String.valueOf(tempNumber), new IDescription(referenceVariableType, false));
            tempNumber++;
        } else if (sem.equals("add")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = operationCheckType(variableType1, id1, variableType2, id2, "mul");
            if (referenceVariableType.equals("float")) {
                codeArray.add(codeSize + ":\n %" + tempNumber + " = fadd " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add(codeSize + ":\n %" + tempNumber + " = add " + referenceVariableType + " %" + id1 + " ,%" + id2);
            }
            semanticStack.push(String.valueOf(tempNumber));
            symbolTable.put(String.valueOf(tempNumber), new IDescription(referenceVariableType, false));
            tempNumber++;
        } else if (sem.equals("sub")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = operationCheckType(variableType1, id1, variableType2, id2, "mul");
            if (referenceVariableType.equals("float")) {
                codeArray.add(codeSize + ":\n %" + tempNumber + " = fsub " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add(codeSize + ":\n %" + tempNumber + " = sub " + referenceVariableType + " %" + id1 + " ,%" + id2);
            }
            semanticStack.push(String.valueOf(tempNumber));
            symbolTable.put(String.valueOf(tempNumber), new IDescription(referenceVariableType, false));
            tempNumber++;
        }
    }

    private boolean assignCheckType(String assignVariableType, String assignId, String variableType) {
        // TODO: Check type during assign variable
        // TODO: Add code to code array for cast
        return true;
    }

    private String operationCheckType(String variableType1, String id1, String variableType2, String id2, String operationType) {
        // TODO: Check operation variable type
        // TODO: Cast variables to reference type
        return "i32";
    }
}
