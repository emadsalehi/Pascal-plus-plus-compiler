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
    private Map<String, FDescription> functionTable = new HashMap<String, FDescription>();
    private List<String> codeArray = new ArrayList<String>();

    public void generateCode(String sem, Symbol symbol) throws Exception {
        int codeSize = codeArray.size();
        if (sem.equals("pushNew")) {
            semanticStack.push((String) symbol.value);
        } else if (sem.equals("pushType")) {
            semanticStack.push(getVariableType(symbol.sym));
        } else if (sem.equals("vdscp") || sem.equals("adscp")) {
            String variableType = semanticStack.pop();
            String id = semanticStack.peek();
            if (sem.equals("vdscp")) {
                symbolTable.put(id, new IDescription(variableType, false));
                String code = codeSize + ":\n\t %" + id + " = alloca " + variableType;
                codeArray.add(code);
            } else {
                StringBuilder arrayVariableType = new StringBuilder(variableType);
                Stack<Integer> sizeList = new Stack<Integer>();
                for (int i = 0; i < tempCounter; i++) {
                    int dimension = Integer.parseInt(semanticStack.pop());
                    arrayVariableType = new StringBuilder("[" + dimension + " x " + arrayVariableType + "]");
                    sizeList.add(dimension);
                }
                symbolTable.put(id, new IDescription(variableType, true));
                IDescription iDescription = new IDescription(arrayVariableType.toString(), true);
                // TODO : Add dimension description  to iDescription
                symbolTable.put(id, iDescription);
                String code = codeSize + ":\n\t %ptr" + id + " = alloca " + arrayVariableType;
                codeArray.add(code);
            }
        } else if (sem.equals("asgnDcl")) {
            String assignId = semanticStack.pop();
            String id = semanticStack.peek();
            String assignVariableType = symbolTable.get(assignId).getType();
            String variableType = symbolTable.get(semanticStack.peek()).getType();
            codeSize++;
            if (!assignCheckType(assignVariableType, assignId, variableType))
                throw new Exception("Wrong assign variable check type");
            codeArray.add(codeSize + ":\n\t store " + variableType + " %" + assignId + ", " + variableType + "* %" + id);
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
                codeArray.add(codeSize + ":\n\t %" + tempNumber + " = fmul " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add(codeSize + ":\n\t %" + tempNumber + " = mul " + referenceVariableType + " %" + id1 + " ,%" + id2);
            }
            semanticStack.push(String.valueOf(tempNumber));
            symbolTable.put(String.valueOf(tempNumber), new IDescription(referenceVariableType, false));
            tempNumber++;
        } else if (sem.equals("div")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = operationCheckType(variableType1, id1, variableType2, id2, "div");
            if (referenceVariableType.equals("float")) {
                codeArray.add(codeSize + ":\n\t %" + tempNumber + " = fdiv " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add(codeSize + ":\n\t %" + tempNumber + " = div " + referenceVariableType + " %" + id1 + " ,%" + id2);
            }
            semanticStack.push(String.valueOf(tempNumber));
            symbolTable.put(String.valueOf(tempNumber), new IDescription(referenceVariableType, false));
            tempNumber++;
        } else if (sem.equals("mod")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = operationCheckType(variableType1, id1, variableType2, id2, "mod");
            if (referenceVariableType.equals("float")) {
                codeArray.add(codeSize + ":\n\t %" + tempNumber + " = frem " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add(codeSize + ":\n\t %" + tempNumber + " = srem " + referenceVariableType + " %" + id1 + " ,%" + id2);
            }
            semanticStack.push(String.valueOf(tempNumber));
            symbolTable.put(String.valueOf(tempNumber), new IDescription(referenceVariableType, false));
            tempNumber++;
        } else if (sem.equals("add")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = operationCheckType(variableType1, id1, variableType2, id2, "add");
            if (referenceVariableType.equals("float")) {
                codeArray.add(codeSize + ":\n\t %" + tempNumber + " = fadd " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add(codeSize + ":\n\t %" + tempNumber + " = add " + referenceVariableType + " %" + id1 + " ,%" + id2);
            }
            semanticStack.push(String.valueOf(tempNumber));
            symbolTable.put(String.valueOf(tempNumber), new IDescription(referenceVariableType, false));
            tempNumber++;
        } else if (sem.equals("sub")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = operationCheckType(variableType1, id1, variableType2, id2, "sub");
            if (referenceVariableType.equals("float")) {
                codeArray.add(codeSize + ":\n\t %" + tempNumber + " = fsub " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add(codeSize + ":\n\t %" + tempNumber + " = sub " + referenceVariableType + " %" + id1 + " ,%" + id2);
            }
            semanticStack.push(String.valueOf(tempNumber));
            symbolTable.put(String.valueOf(tempNumber), new IDescription(referenceVariableType, false));
            tempNumber++;
        } else if (sem.equals("less")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = operationCheckType(variableType1, id1, variableType2, id2, "less");
            if (referenceVariableType.equals("float")) {
                codeArray.add(codeSize + ":\n\t %" + tempNumber + " = fcmp slt " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add(codeSize + ":\n\t %" + tempNumber + " = icmp slt " + referenceVariableType + " %" + id1 + " ,%" + id2);
            }
            semanticStack.push(String.valueOf(tempNumber));
            symbolTable.put(String.valueOf(tempNumber), new IDescription(referenceVariableType, false));
            tempNumber++;
        } else if (sem.equals("greater")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = operationCheckType(variableType1, id1, variableType2, id2, "greater");
            if (referenceVariableType.equals("float")) {
                codeArray.add(codeSize + ":\n\t %" + tempNumber + " = fcmp sgt " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add(codeSize + ":\n\t %" + tempNumber + " = icmp sgt " + referenceVariableType + " %" + id1 + " ,%" + id2);
            }
            semanticStack.push(String.valueOf(tempNumber));
            symbolTable.put(String.valueOf(tempNumber), new IDescription(referenceVariableType, false));
            tempNumber++;
        } else if (sem.equals("lessEq")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = operationCheckType(variableType1, id1, variableType2, id2, "lessEq");
            if (referenceVariableType.equals("float")) {
                codeArray.add(codeSize + ":\n\t %" + tempNumber + " = fcmp sle " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add(codeSize + ":\n\t %" + tempNumber + " = icmp sle " + referenceVariableType + " %" + id1 + " ,%" + id2);
            }
            semanticStack.push(String.valueOf(tempNumber));
            symbolTable.put(String.valueOf(tempNumber), new IDescription(referenceVariableType, false));
            tempNumber++;
        } else if (sem.equals("greaterEq")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = operationCheckType(variableType1, id1, variableType2, id2, "greaterEq");
            if (referenceVariableType.equals("float")) {
                codeArray.add(codeSize + ":\n\t %" + tempNumber + " = fcmp gle " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add(codeSize + ":\n\t %" + tempNumber + " = icmp gle " + referenceVariableType + " %" + id1 + " ,%" + id2);
            }
            semanticStack.push(String.valueOf(tempNumber));
            symbolTable.put(String.valueOf(tempNumber), new IDescription(referenceVariableType, false));
            tempNumber++;
        } else if (sem.equals("eq")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = operationCheckType(variableType1, id1, variableType2, id2, "eq");
            if (referenceVariableType.equals("float")) {
                codeArray.add(codeSize + ":\n\t %" + tempNumber + " = fcmp eq " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add(codeSize + ":\n\t %" + tempNumber + " = icmp eq " + referenceVariableType + " %" + id1 + " ,%" + id2);
            }
            semanticStack.push(String.valueOf(tempNumber));
            symbolTable.put(String.valueOf(tempNumber), new IDescription(referenceVariableType, false));
            tempNumber++;
        } else if (sem.equals("notEq")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = operationCheckType(variableType1, id1, variableType2, id2, "notEq");
            if (referenceVariableType.equals("float")) {
                codeArray.add(codeSize + ":\n\t %" + tempNumber + " = fcmp ne " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add(codeSize + ":\n\t %" + tempNumber + " = icmp ne " + referenceVariableType + " %" + id1 + " ,%" + id2);
            }
            semanticStack.push(String.valueOf(tempNumber));
            symbolTable.put(String.valueOf(tempNumber), new IDescription(referenceVariableType, false));
            tempNumber++;
        } else if (sem.equals("bitwiseAnd")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = operationCheckType(variableType1, id1, variableType2, id2, "bitwiseAnd");
            codeArray.add(codeSize + ":\n\t %" + tempNumber + " = and " + referenceVariableType + " %" + id1 + " ,%" + id2);
            semanticStack.push(String.valueOf(tempNumber));
            symbolTable.put(String.valueOf(tempNumber), new IDescription(referenceVariableType, false));
            tempNumber++;
        } else if (sem.equals("xor")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = operationCheckType(variableType1, id1, variableType2, id2, "xor");
            codeArray.add(codeSize + ":\n\t %" + tempNumber + " = xor " + referenceVariableType + " %" + id1 + " ,%" + id2);
            semanticStack.push(String.valueOf(tempNumber));
            symbolTable.put(String.valueOf(tempNumber), new IDescription(referenceVariableType, false));
            tempNumber++;
        } else if (sem.equals("bitwiseOr")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = operationCheckType(variableType1, id1, variableType2, id2, "bitwiseOr");
            codeArray.add(codeSize + ":\n\t %" + tempNumber + " = or " + referenceVariableType + " %" + id1 + " ,%" + id2);
            semanticStack.push(String.valueOf(tempNumber));
            symbolTable.put(String.valueOf(tempNumber), new IDescription(referenceVariableType, false));
            tempNumber++;
        } else if (sem.equals("logicalAnd")) {
            // TODO: Implement logical and
        } else if (sem.equals("logicalOr")) {
            // TODO: Implement logical or
        } else if (sem.equals("createJump")) {
            String id = semanticStack.pop();
            codeArray.add(codeSize + ":\n\t br i1 %" + id + ", label %" + (codeSize + 1) + ", label %");
            semanticStack.push(String.valueOf(codeSize));
        } else if (sem.equals("compJp")) {
            codeArray.add(codeSize + ":\n\t ");
            codeSize++;
            int jumpIndex = Integer.parseInt(semanticStack.pop());
            String code = codeArray.get(jumpIndex).concat(String.valueOf(codeSize));
            codeArray.set(jumpIndex, code);
        } else if (sem.equals("elseJp")) {
            int jumpIndex = codeSize - 2;
            codeArray.set(jumpIndex, codeArray.get(jumpIndex).concat("br label %"));
            semanticStack.push(String.valueOf(jumpIndex));
        } else if (sem.equals("compElseJp")) {
            int jumpIndex = Integer.parseInt(semanticStack.pop());
            codeArray.set(jumpIndex, codeArray.get(jumpIndex).concat(String.valueOf(codeSize)));
        } else if (sem.equals("pushWhileLabel")) {
            semanticStack.push(String.valueOf(codeSize));
        } else if (sem.equals("createWhileJump")) {
            int jumpLabel = Integer.parseInt(semanticStack.pop());
            String id = semanticStack.pop();
            codeArray.add(codeSize + ":\n\t br i1 %" + id + ", label %" + jumpLabel + ", label %" + (codeSize + 1));
        } else if (sem.equals("incrementCnt")) {
            tempCounter++;
        } else if (sem.equals("pushFunctionId")) {
            String id = (String) symbol.value;
            semanticStack.push(id);
            functionTable.put(id, new FDescription());
        } else if (sem.equals("addNumberOfArguments")) {
            functionTable.get(semanticStack.peek()).setNumberOfArguments(tempCounter);
        } else if (sem.equals("pushFunctionArgumentId")) {
            functionTable.get(semanticStack.peek()).addParameterName((String) symbol.value);
        } else if (sem.equals("pushFunctionArgumentType")) {
            String variableType = semanticStack.pop();
            functionTable.get(semanticStack.peek()).addParameterVariableType(variableType);
        } else if (sem.equals("pushFunctionArrayArgumentType")) {
            String variableType = semanticStack.pop();
            StringBuilder arrayVariableType = new StringBuilder(variableType);
            for (int i = 0; i < tempCounter; i++) {
                int dimension = Integer.parseInt(semanticStack.pop());
                arrayVariableType = new StringBuilder("[" + dimension + " x " + arrayVariableType + "]");
            }
            functionTable.get(semanticStack.peek()).addParameterVariableType(arrayVariableType.toString());
        } else if (sem.equals("functionGenerateCode")) {
            String variableType = semanticStack.pop();
            functionTable.get(semanticStack.peek()).setReturnType(variableType);
            generateFunctionCode(semanticStack.pop());
        } else if (sem.equals("procedureGenerateCode")) {
            functionTable.get(semanticStack.peek()).setReturnType("void");
            generateFunctionCode(semanticStack.pop());
        } else if (sem.equals("endFunction")) {
            codeArray.add("}");
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

    private String getVariableType(int typeSymbol) {
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
        return variableType;
    }

    private void generateFunctionCode(String functionId) {
        String functionReturnType = functionTable.get(functionId).getReturnType();
        StringBuilder code = new StringBuilder("define " + functionReturnType + " @" + functionId + "(");
        List<String> variableTypes = functionTable.get(functionId).getParameterVariableTypes();
        List<String> variableIds = functionTable.get(functionId).getParameterIds();
        for (int i = 0 ; i < functionTable.get(functionId).getNumberOfArguments() ; i++) {
            code.append(variableTypes.get(i)).append(" %").append(variableIds.get(i));
            if (i < functionTable.get(functionId).getNumberOfArguments() - 1)
                code.append(", ");
        }
        code.append(") {");
        codeArray.add(code.toString());
        codeArray.add("entry" + functionId + ":");
    }
}
