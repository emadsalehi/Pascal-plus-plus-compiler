package code_generator;

import java_cup.runtime.Symbol;

import java.util.*;

public class CodeGenerator {

    private int stp;
    private int tempNumber = 1;
    private int tempCounter = 0;
    private String tempId = "";
    private Stack<String> semanticStack = new Stack<String>();
    private Map<String, IDescription> symbolTable = new HashMap<String, IDescription>();
    private Map<String, FDescription> functionTable = new HashMap<String, FDescription>();
    private List<String> codeArray = new ArrayList<String>();


    public CodeGenerator() {
        codeArray.add("@.str.1 = private unnamed_addr constant [3 x i8] c\"%d\\00\", align 1");
        codeArray.add("@.str.2 = private unnamed_addr constant [3 x i8] c\"%c\\00\", align 1");
        codeArray.add("@.str.3 = private unnamed_addr constant [3 x i8] c\"%f\\00\", align 1");
        codeArray.add("declare i32 @scanf(i8*, ...)");
        codeArray.add("declare i32 @printf(i8*, ...)");
    }

    public void generateCode(String sem, Symbol symbol) throws Exception {
        int codeSize = codeArray.size();
        sem = sem.substring(1);
        if (sem.equals("pushNew")) {
            semanticStack.push((String) symbol.value);
            tempId = (String) symbol.value;
        } else if (sem.equals("pushType")) {
            semanticStack.push(getVariableType(symbol.sym));
        } else if (sem.equals("vdscp") || sem.equals("adscp")) {
            String variableType = semanticStack.pop();
            String id = semanticStack.peek();
            if (sem.equals("vdscp")) {
                symbolTable.put(id, new IDescription(variableType, false));
                String code = ";label" + codeSize + ":\n\t %" + id + " = alloca " + variableType;
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
                String code = ";label" + codeSize + ":\n\t %" + id + " = alloca " + arrayVariableType;
                codeArray.add(code);
            }
        } else if (sem.equals("asgnDcl") | sem.equals("asgnArray")) {
            String assignId = semanticStack.pop();
            String id = semanticStack.peek();
            String assignVariableType = symbolTable.get(assignId).getType();
            String variableType = symbolTable.get(semanticStack.peek()).getType();
            codeSize++;
            if (!assignCheckType(assignVariableType, assignId, variableType))
                throw new Exception("Wrong assign variable check type");
            codeArray.add(";label" + codeSize + ":\n\tstore " + variableType + " %" + assignId + ", " + variableType + "* %" + id);
            if (sem.equals("asgnArray"))
                semanticStack.pop();
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
            String referenceVariableType = booleanOperationCheckType(variableType1, id1, variableType2, id2, "mul");
            if (referenceVariableType.equals("float")) {
                codeArray.add(";label" + codeSize + ":\n\t %" + tempNumber + " = fmul " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add(";label" + codeSize + ":\n\t %" + tempNumber + " = mul " + referenceVariableType + " %" + id1 + " ,%" + id2);
            }
            semanticStack.push(String.valueOf(tempNumber));
            symbolTable.put(String.valueOf(tempNumber), new IDescription(referenceVariableType, false));
            tempNumber++;
        } else if (sem.equals("div")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = booleanOperationCheckType(variableType1, id1, variableType2, id2, "div");
            if (referenceVariableType.equals("float")) {
                codeArray.add(";label" + codeSize + ":\n\t %" + tempNumber + " = fdiv " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add(";label" + codeSize + ":\n\t %" + tempNumber + " = sdiv " + referenceVariableType + " %" + id1 + " ,%" + id2);
            }
            semanticStack.push(String.valueOf(tempNumber));
            symbolTable.put(String.valueOf(tempNumber), new IDescription(referenceVariableType, false));
            tempNumber++;
        } else if (sem.equals("mod")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = booleanOperationCheckType(variableType1, id1, variableType2, id2, "mod");
            if (referenceVariableType.equals("float")) {
                codeArray.add(";label" + codeSize + ":\n\t %" + tempNumber + " = frem " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add(";label" + codeSize + ":\n\t %" + tempNumber + " = srem " + referenceVariableType + " %" + id1 + " ,%" + id2);
            }
            semanticStack.push(String.valueOf(tempNumber));
            symbolTable.put(String.valueOf(tempNumber), new IDescription(referenceVariableType, false));
            tempNumber++;
        } else if (sem.equals("add")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = booleanOperationCheckType(variableType1, id1, variableType2, id2, "add");
            if (referenceVariableType.equals("float")) {
                codeArray.add(";label" + codeSize + ":\n\t %" + tempNumber + " = fadd " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add(";label" + codeSize + ":\n\t %" + tempNumber + " = add " + referenceVariableType + " %" + id1 + " ,%" + id2);
            }
            semanticStack.push(String.valueOf(tempNumber));
            symbolTable.put(String.valueOf(tempNumber), new IDescription(referenceVariableType, false));
            tempNumber++;
        } else if (sem.equals("sub")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = booleanOperationCheckType(variableType1, id1, variableType2, id2, "sub");
            if (referenceVariableType.equals("float")) {
                codeArray.add(";label" + codeSize + ":\n\t %" + tempNumber + " = fsub " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add(";label" + codeSize + ":\n\t %" + tempNumber + " = sub " + referenceVariableType + " %" + id1 + " ,%" + id2);
            }
            semanticStack.push(String.valueOf(tempNumber));
            symbolTable.put(String.valueOf(tempNumber), new IDescription(referenceVariableType, false));
            tempNumber++;
        } else if (sem.equals("less")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = booleanOperationCheckType(variableType1, id1, variableType2, id2, "less");
            if (referenceVariableType.equals("float")) {
                codeArray.add(";label" + codeSize + ":\n\t %" + tempNumber + " = fcmp slt " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add(";label" + codeSize + ":\n\t %" + tempNumber + " = icmp slt " + referenceVariableType + " %" + id1 + " ,%" + id2);
            }
            semanticStack.push(String.valueOf(tempNumber));
            symbolTable.put(String.valueOf(tempNumber), new IDescription(referenceVariableType, false));
            tempNumber++;
        } else if (sem.equals("greater")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = booleanOperationCheckType(variableType1, id1, variableType2, id2, "greater");
            if (referenceVariableType.equals("float")) {
                codeArray.add(";label" + codeSize + ":\n\t %" + tempNumber + " = fcmp sgt " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add(";label" + codeSize + ":\n\t %" + tempNumber + " = icmp sgt " + referenceVariableType + " %" + id1 + " ,%" + id2);
            }
            semanticStack.push(String.valueOf(tempNumber));
            symbolTable.put(String.valueOf(tempNumber), new IDescription(referenceVariableType, false));
            tempNumber++;
        } else if (sem.equals("lessEq")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = booleanOperationCheckType(variableType1, id1, variableType2, id2, "lessEq");
            if (referenceVariableType.equals("float")) {
                codeArray.add(";label" + codeSize + ":\n\t %" + tempNumber + " = fcmp sle " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add(";label" + codeSize + ":\n\t %" + tempNumber + " = icmp sle " + referenceVariableType + " %" + id1 + " ,%" + id2);
            }
            semanticStack.push(String.valueOf(tempNumber));
            symbolTable.put(String.valueOf(tempNumber), new IDescription(referenceVariableType, false));
            tempNumber++;
        } else if (sem.equals("greaterEq")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = booleanOperationCheckType(variableType1, id1, variableType2, id2, "greaterEq");
            if (referenceVariableType.equals("float")) {
                codeArray.add(";label" + codeSize + ":\n\t %" + tempNumber + " = fcmp gle " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add(";label" + codeSize + ":\n\t %" + tempNumber + " = icmp gle " + referenceVariableType + " %" + id1 + " ,%" + id2);
            }
            semanticStack.push(String.valueOf(tempNumber));
            symbolTable.put(String.valueOf(tempNumber), new IDescription(referenceVariableType, false));
            tempNumber++;
        } else if (sem.equals("eq")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = booleanOperationCheckType(variableType1, id1, variableType2, id2, "eq");
            if (referenceVariableType.equals("float")) {
                codeArray.add(";label" + codeSize + ":\n\t %" + tempNumber + " = fcmp eq " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add(";label" + codeSize + ":\n\t %" + tempNumber + " = icmp eq " + referenceVariableType + " %" + id1 + " ,%" + id2);
            }
            semanticStack.push(String.valueOf(tempNumber));
            symbolTable.put(String.valueOf(tempNumber), new IDescription(referenceVariableType, false));
            tempNumber++;
        } else if (sem.equals("notEq")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = booleanOperationCheckType(variableType1, id1, variableType2, id2, "notEq");
            if (referenceVariableType.equals("float")) {
                codeArray.add(";label" + codeSize + ":\n\t %" + tempNumber + " = fcmp ne " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add(";label" + codeSize + ":\n\t %" + tempNumber + " = icmp ne " + referenceVariableType + " %" + id1 + " ,%" + id2);
            }
            semanticStack.push(String.valueOf(tempNumber));
            symbolTable.put(String.valueOf(tempNumber), new IDescription(referenceVariableType, false));
            tempNumber++;
        } else if (sem.equals("bitwiseAnd")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = booleanOperationCheckType(variableType1, id1, variableType2, id2, "bitwiseAnd");
            codeArray.add(";label" + codeSize + ":\n\t %" + tempNumber + " = and " + referenceVariableType + " %" + id1 + " ,%" + id2);
            semanticStack.push(String.valueOf(tempNumber));
            symbolTable.put(String.valueOf(tempNumber), new IDescription(referenceVariableType, false));
            tempNumber++;
        } else if (sem.equals("xor")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = booleanOperationCheckType(variableType1, id1, variableType2, id2, "xor");
            codeArray.add(";label" + codeSize + ":\n\t %" + tempNumber + " = xor " + referenceVariableType + " %" + id1 + " ,%" + id2);
            semanticStack.push(String.valueOf(tempNumber));
            symbolTable.put(String.valueOf(tempNumber), new IDescription(referenceVariableType, false));
            tempNumber++;
        } else if (sem.equals("bitwiseOr")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = booleanOperationCheckType(variableType1, id1, variableType2, id2, "bitwiseOr");
            codeArray.add(";label" + codeSize + ":\n\t %" + tempNumber + " = or " + referenceVariableType + " %" + id1 + " ,%" + id2);
            semanticStack.push(String.valueOf(tempNumber));
            symbolTable.put(String.valueOf(tempNumber), new IDescription(referenceVariableType, false));
            tempNumber++;
        } else if (sem.equals("logicalAnd")) {
            // TODO: Implement logical and
        } else if (sem.equals("logicalOr")) {
            // TODO: Implement logical or
        } else if (sem.equals("createJump")) {
            String id = semanticStack.pop();
            codeArray.add(";label" + codeSize + ":\n\t br i1 %" + id + ", label %" + ("label" + codeSize + 1) + ", label %");
            semanticStack.push(String.valueOf(codeSize));
        } else if (sem.equals("compJp")) {
            codeArray.add(";label" + codeSize + ":\n\t ");
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
            codeArray.set(jumpIndex, codeArray.get(jumpIndex).concat(String.valueOf("label" + codeSize)));
        } else if (sem.equals("pushWhileLabel")) {
            semanticStack.push(String.valueOf(codeSize));
        } else if (sem.equals("createWhileJump")) {
            int jumpLabel = Integer.parseInt(semanticStack.pop());
            String id = semanticStack.pop();
            codeArray.add(";label" + codeSize + ":\n\t br i1 %" + id + ", label %" + jumpLabel + ", label %" + ("label" + codeSize + 1));
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
        } else if (sem.equals("callWrite")) {
            String id = semanticStack.pop();
            String variableType = symbolTable.get(id).getType();
            String printParameter = getPrintParameter(variableType);
            codeArray.add(";label" + codeSize + ":\n\t %" + tempNumber + " = call i32 (i8*, ...) @printf(" + printParameter + variableType + " %" + id + ")");
            symbolTable.put(String.valueOf(tempNumber), new IDescription("i32", false));
            tempNumber++;
        } else if (sem.equals("callRead")) {
            String id = (String) symbol.value;
            String variableType = symbolTable.get(id).getType();
            String printParameter = getPrintParameter(variableType);
            symbolTable.put(String.valueOf(tempNumber), new IDescription("i32", false));
            codeArray.add(";label" + codeSize + ":\n\t %" + tempNumber + " = call i32 (i8*, ...) @scanf(" + printParameter + variableType + "* %" + id + ")");
            tempNumber++;
        } else if (sem.equals("pushConstant")) {
            String variableType = " ";
            switch (symbol.sym) {
                case 50:
                    variableType = "i1";
                    break;
                case 51:
                    variableType = "i8";
                    break;
                case 52:
                    variableType = "i32";
                    break;
                case 53:
                    variableType = "float";
                    break;
                case 54:
                    variableType = "i8*";
                    break;
            }
            codeArray.add(";label" + codeSize + ":\n\t %" + tempNumber + " = alloca " + variableType);
            codeSize++;
            codeArray.add(";label" + codeSize + ":\n\t store " + variableType + " " + symbol.value + ", " + variableType + "* %" + tempNumber);
            codeSize++;
            tempNumber++;
            codeArray.add(";label" + codeSize + ":\n\t %" + tempNumber + " = load " + variableType + ", " + variableType + "* %" + (tempNumber - 1));
            symbolTable.put(String.valueOf(tempNumber), new IDescription(variableType, false));
            semanticStack.add(String.valueOf(tempNumber));
            tempNumber++;
        } else if (sem.equals("returnId")) {
            String id = (String) symbol.value;
            String variableType = symbolTable.get(id).getType();
            codeArray.add(";label" + codeSize + ":\n\t %" + tempNumber + " = load " + variableType + ", " + variableType + "* %" + (tempNumber - 1));
            symbolTable.put(String.valueOf(tempNumber), new IDescription(variableType, false));
            codeSize++;
            codeArray.add(";label" + codeSize + ":\n\tret " + variableType + " %" + tempNumber);
            tempNumber++;
        } else if (sem.equals("returnConstant")) {
            String id = semanticStack.pop();
            String variableType = symbolTable.get(id).getType();
            codeArray.add(";label" + codeSize + ":\n\tret " + variableType + " %" + id);
            tempNumber++;
        } else if (sem.equals("pushUnary")) {
            semanticStack.push(String.valueOf(symbol.sym));
        } else if (sem.equals("applyUnaryOp")) {
            String id = semanticStack.pop();
            String variableType = symbolTable.get(id).getType();
            int unaryOpCode = Integer.parseInt(semanticStack.pop());
            if (unaryOpCode == 24) {
                if (!unaryOperationCheckType(variableType, id, "minus"))
                    throw new Exception("Wrong variable type in minus");
                if (variableType.equals("float")) {
                    codeArray.add(";label" + codeSize + ":\n\t %" + tempNumber + " = fsub 0, %" + id);
                } else {
                    codeArray.add(";label" + codeSize + ":\n\t%" + tempNumber + " = sub 0, %" + id);
                }
                semanticStack.push(String.valueOf(tempNumber));
                symbolTable.put(String.valueOf(tempNumber), new IDescription(variableType, false));
                tempNumber++;
            } else {
                if (!unaryOperationCheckType(variableType, id, "not"))
                    throw new Exception("Wrong variable type in not");
                codeArray.add(";label" + codeSize + ":\n\t%" + tempNumber + " = xor -1, %" + id);
                semanticStack.push(String.valueOf(tempNumber));
                symbolTable.put(String.valueOf(tempNumber), new IDescription(variableType, false));
                tempNumber++;
            }
        } else if (sem.equals("pushIdValue")) {
            tempId = (String) symbol.value;
            String variableType = symbolTable.get(tempId).getType();
            codeArray.add(";label" + codeSize + ":\n\t%" + tempNumber + " = load " + variableType + ", " + variableType + "* %" + tempId);
            semanticStack.push(String.valueOf(tempNumber));
            symbolTable.put(String.valueOf(tempNumber), new IDescription(variableType, false));
            tempNumber++;
        } else if (sem.equals("pushArrayId")) {
            semanticStack.pop();
            semanticStack.push(tempId);
            tempCounter = 0;
        } else if (sem.equals("pushArrayElementPointer")) {
            Stack<String> arrayElements = new Stack<String>();
            for (int i = 0; i < tempCounter; i++) {
                if (!symbolTable.get(semanticStack.peek()).getType().equals("i32"))
                    throw new Exception("Wrong array index");
                arrayElements.add(semanticStack.pop());
            }
            String arrayId = semanticStack.pop();
            String variableType = symbolTable.get(arrayId).getType();
            StringBuilder loadPointer = new StringBuilder("getelementptr inbounds " + variableType + ", " + variableType + "* %" + arrayId + ", i32 0");
            while (!arrayElements.isEmpty()) {
                loadPointer.append(", i32 %").append(arrayElements.pop());
            }
            codeArray.add(";label" + codeSize + ":\n\t%" + tempNumber + " = " + loadPointer);
            semanticStack.push(String.valueOf(tempNumber));
            symbolTable.put(String.valueOf(tempNumber), new IDescription("i32", false));
            tempNumber++;
        } else if (sem.equals("loadValue")) {
            String id = semanticStack.pop();
            String variableType = symbolTable.get(id).getType();
            codeArray.add(";label" + codeSize + ":\n\t %" + tempNumber + " = load " + variableType + ", " + variableType + "* %" + id);
            symbolTable.put(String.valueOf(tempNumber), new IDescription(variableType, false));
            semanticStack.add(String.valueOf(tempNumber));
            tempNumber++;
        }
    }

    private boolean assignCheckType(String assignVariableType, String assignId, String variableType) {
        // TODO: Check type during assign variable
        // TODO: Add code to code array for cast
        return true;
    }

    private String booleanOperationCheckType(String variableType1, String id1, String variableType2, String id2, String operationType) {
        // TODO: Check operation variable type
        // TODO: Cast variables to reference type
        return "i32";
    }

    private boolean unaryOperationCheckType(String variableType, String id, String operationType) {
        // TODO: Check operation variable type
        // TODO: Cast variables to reference type
        return true;
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

    private String getPrintParameter(String variableType) {
        String printParameter = " ";
        if (variableType.equals("i32")) {
            printParameter = "i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.str.1, i32 0, i32 0), ";
        } else if (variableType.equals("i8")) {
            printParameter = "i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.str.2, i32 0, i32 0), ";
        } else if (variableType.equals("float")) {
            printParameter = "i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.str.1, i32 0, i32 0), ";
        } else if (variableType.equals("i8*")) {
            printParameter = " ";
        }
        return printParameter;
    }

    private void generateFunctionCode(String functionId) {
        String functionReturnType = functionTable.get(functionId).getReturnType();
        StringBuilder code = new StringBuilder("define " + functionReturnType + " @" + functionId + "(");
        List<String> variableTypes = functionTable.get(functionId).getParameterVariableTypes();
        List<String> variableIds = functionTable.get(functionId).getParameterIds();
        for (int i = 0; i < functionTable.get(functionId).getNumberOfArguments(); i++) {
            code.append(variableTypes.get(i)).append(" %").append(variableIds.get(i));
            if (i < functionTable.get(functionId).getNumberOfArguments() - 1)
                code.append(", ");
        }
        code.append(") {");
        codeArray.add(code.toString());
        codeArray.add(";entry" + functionId + ":");
    }

    public void printAllCode() {
        for (String code : codeArray) {
            System.out.println(code);
        }
    }

}
