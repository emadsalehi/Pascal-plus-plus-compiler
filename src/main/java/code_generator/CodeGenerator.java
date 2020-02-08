package code_generator;

import java_cup.runtime.Symbol;

import java.util.*;

public class CodeGenerator {

    private int varTempNumber = 1;
    private int labelTempNumber = 0;
    private int tempCounter = 0;
    private int globalStrCounter = 5;
    private Stack<String> tempId = new Stack<String>();
    private Stack<String> semanticStack = new Stack<String>();
    private Map<String, IDescription> symbolTable = new HashMap<String, IDescription>();
    private Map<String, FDescription> functionTable = new HashMap<String, FDescription>();
    private List<String> codeArray = new ArrayList<String>();
    private List<String> globalStringDefinition = new ArrayList<String>();


    public CodeGenerator() {
        codeArray.add("@.str.1 = private unnamed_addr constant [3 x i8] c\"%d\\00\", align 1");
        codeArray.add("@.str.2 = private unnamed_addr constant [3 x i8] c\"%c\\00\", align 1");
        codeArray.add("@.str.3 = private unnamed_addr constant [3 x i8] c\"%f\\00\", align 1");
        codeArray.add("@.str.4 = private unnamed_addr constant [3 x i8] c\"%s\\00\", align 1");
        codeArray.add("declare i32 @scanf(i8*, ...)");
        codeArray.add("declare i32 @printf(i8*, ...)");
    }

    public void generateCode(String sem, Symbol symbol) throws Exception {
        int codeSize = codeArray.size();
        sem = sem.substring(1);
        if (sem.equals("pushNew")) {
            semanticStack.push((String) symbol.value);
            codeArray.add(" ");
            tempId.push((String) symbol.value);
        } else if (sem.equals("pushType")) {
            semanticStack.push(getVariableType(symbol.sym));
        } else if (sem.equals("vdscp") || sem.equals("adscp")) {
            String variableType = semanticStack.pop();
            if (sem.equals("vdscp")) {
                String id = semanticStack.peek();
                symbolTable.put(id, new IDescription(variableType, false));
                String code = "\t%" + id + " = alloca " + variableType;
                codeArray.add(code);
            } else {
                StringBuilder arrayVariableType = new StringBuilder(variableType);
                Stack<Integer> sizeList = new Stack<Integer>();
                for (int i = 0; i < tempCounter; i++) {
                    int dimension = Integer.parseInt(semanticStack.pop());
                    arrayVariableType = new StringBuilder("[" + dimension + " x " + arrayVariableType + "]");
                    sizeList.add(dimension);
                }
                String id = semanticStack.peek();
                symbolTable.put(id, new IDescription(variableType, true));
                IDescription iDescription = new IDescription(arrayVariableType.toString(), true);
                // TODO : Add dimension description  to iDescription
                symbolTable.put(id, iDescription);
                String code = "\t%" + id + " = alloca " + arrayVariableType;
                codeArray.add(code);
            }
        } else if (sem.equals("asgnDcl") | sem.equals("asgnArray") | sem.equals("asgnDclPop")) {
            String assignId = semanticStack.pop();
            String id = semanticStack.peek();
            String assignVariableType = symbolTable.get(assignId).getType();
            String variableType = symbolTable.get(semanticStack.peek()).getType();
            if (!assignCheckType(assignVariableType, assignId, variableType))
                throw new Exception("Wrong assign variable check type");
            codeArray.add("\tstore " + variableType + " %" + assignId + ", " + variableType + "* %" + id);
            if (sem.equals("asgnArray") | sem.equals("asgnDclPop"))
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
                codeArray.add("\t%_" + varTempNumber + " = fmul " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add("\t%_" + varTempNumber + " = mul " + referenceVariableType + " %" + id1 + " ,%" + id2);
            }
            semanticStack.push("_" + varTempNumber);
            symbolTable.put("_" + varTempNumber, new IDescription(referenceVariableType, false));
            varTempNumber++;

        } else if (sem.equals("div")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = booleanOperationCheckType(variableType1, id1, variableType2, id2, "div");
            if (referenceVariableType.equals("float")) {
                codeArray.add("\t%_" + varTempNumber + " = fdiv " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add("\t%_" + varTempNumber + " = sdiv " + referenceVariableType + " %" + id1 + " ,%" + id2);
            }
            semanticStack.push("_" + varTempNumber);
            symbolTable.put("_" + varTempNumber, new IDescription(referenceVariableType, false));
            varTempNumber++;
        } else if (sem.equals("mod")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = booleanOperationCheckType(variableType1, id1, variableType2, id2, "mod");
            if (referenceVariableType.equals("float")) {
                codeArray.add("\t%_" + varTempNumber + " = frem " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add("\t%_" + varTempNumber + " = srem " + referenceVariableType + " %" + id1 + " ,%" + id2);
            }
            semanticStack.push("_" + varTempNumber);
            symbolTable.put("_" + varTempNumber, new IDescription(referenceVariableType, false));
            varTempNumber++;
        } else if (sem.equals("add")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = booleanOperationCheckType(variableType1, id1, variableType2, id2, "add");
            if (referenceVariableType.equals("float")) {
                codeArray.add("\t%_" + varTempNumber + " = fadd " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add("\t%_" + varTempNumber + " = add " + referenceVariableType + " %" + id1 + " ,%" + id2);
            }
            semanticStack.push("_" + varTempNumber);
            symbolTable.put("_" + varTempNumber, new IDescription(referenceVariableType, false));
            varTempNumber++;

        } else if (sem.equals("sub")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = booleanOperationCheckType(variableType1, id1, variableType2, id2, "sub");
            if (referenceVariableType.equals("float")) {
                codeArray.add("\t%_" + varTempNumber + " = fsub " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add("\t%_" + varTempNumber + " = sub " + referenceVariableType + " %" + id1 + " ,%" + id2);
            }
            semanticStack.push("_" + varTempNumber);
            symbolTable.put("_" + varTempNumber, new IDescription(referenceVariableType, false));
            varTempNumber++;

        } else if (sem.equals("less")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = booleanOperationCheckType(variableType1, id1, variableType2, id2, "less");
            if (referenceVariableType.equals("float")) {
                codeArray.add("\t%_" + varTempNumber + " = fcmp slt " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add("\t%_" + varTempNumber + " = icmp slt " + referenceVariableType + " %" + id1 + ", %" + id2);
            }
            semanticStack.push("_" + varTempNumber);
            symbolTable.put("_" + varTempNumber, new IDescription(referenceVariableType, false));
            varTempNumber++;

        } else if (sem.equals("greater")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = booleanOperationCheckType(variableType1, id1, variableType2, id2, "greater");
            if (referenceVariableType.equals("float")) {
                codeArray.add("\t%_" + varTempNumber + " = fcmp sgt " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add("\t%_" + varTempNumber + " = icmp sgt " + referenceVariableType + " %" + id1 + ", %" + id2);
            }
            semanticStack.push("_" + varTempNumber);
            symbolTable.put("_" + varTempNumber, new IDescription(referenceVariableType, false));
            varTempNumber++;

        } else if (sem.equals("lessEq")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = booleanOperationCheckType(variableType1, id1, variableType2, id2, "lessEq");
            if (referenceVariableType.equals("float")) {
                codeArray.add("\t%_" + varTempNumber + " = fcmp sle " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add("\t%_" + varTempNumber + " = icmp sle " + referenceVariableType + " %" + id1 + ", %" + id2);
            }
            semanticStack.push("_" + varTempNumber);
            symbolTable.put("_" + varTempNumber, new IDescription(referenceVariableType, false));
            varTempNumber++;

        } else if (sem.equals("greaterEq")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = booleanOperationCheckType(variableType1, id1, variableType2, id2, "greaterEq");
            if (referenceVariableType.equals("float")) {
                codeArray.add("\t%_" + varTempNumber + " = fcmp gle " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add("\t%_" + varTempNumber + " = icmp gle " + referenceVariableType + " %" + id1 + ", %" + id2);
            }
            semanticStack.push("_" + varTempNumber);
            symbolTable.put("_" + varTempNumber, new IDescription(referenceVariableType, false));
            varTempNumber++;

        } else if (sem.equals("eq")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = booleanOperationCheckType(variableType1, id1, variableType2, id2, "eq");
            if (referenceVariableType.equals("float")) {
                codeArray.add("\t%_" + varTempNumber + " = fcmp eq " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add("\t%_" + varTempNumber + " = icmp eq " + referenceVariableType + " %" + id1 + ", %" + id2);
            }
            semanticStack.push("_" + varTempNumber);
            symbolTable.put("_" + varTempNumber, new IDescription(referenceVariableType, false));
            varTempNumber++;

        } else if (sem.equals("notEq")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = booleanOperationCheckType(variableType1, id1, variableType2, id2, "notEq");
            if (referenceVariableType.equals("float")) {
                codeArray.add("\t%_" + varTempNumber + " = fcmp ne " + referenceVariableType + " %" + id1 + ", %" + id2);
            } else {
                codeArray.add("\t%_" + varTempNumber + " = icmp ne " + referenceVariableType + " %" + id1 + ", %" + id2);
            }
            semanticStack.push("_" + varTempNumber);
            symbolTable.put("_" + varTempNumber, new IDescription(referenceVariableType, false));
            varTempNumber++;

        } else if (sem.equals("bitwiseAnd")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = booleanOperationCheckType(variableType1, id1, variableType2, id2, "bitwiseAnd");
            codeArray.add("\t%_" + varTempNumber + " = and " + referenceVariableType + " %" + id1 + ", %" + id2);
            semanticStack.push("_" + varTempNumber);
            symbolTable.put("_" + varTempNumber, new IDescription(referenceVariableType, false));
            varTempNumber++;

        } else if (sem.equals("xor")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = booleanOperationCheckType(variableType1, id1, variableType2, id2, "xor");
            codeArray.add("\t%_" + varTempNumber + " = xor " + referenceVariableType + " %" + id1 + ", %" + id2);
            semanticStack.push("_" + varTempNumber);
            symbolTable.put("_" + varTempNumber, new IDescription(referenceVariableType, false));
            varTempNumber++;

        } else if (sem.equals("bitwiseOr")) {
            String id2 = semanticStack.pop();
            String id1 = semanticStack.pop();
            String variableType1 = symbolTable.get(id1).getType();
            String variableType2 = symbolTable.get(id2).getType();
            String referenceVariableType = booleanOperationCheckType(variableType1, id1, variableType2, id2, "bitwiseOr");
            codeArray.add("\t%_" + varTempNumber + " = or " + referenceVariableType + " %" + id1 + ", %" + id2);
            semanticStack.push("_" + varTempNumber);
            symbolTable.put("_" + varTempNumber, new IDescription(referenceVariableType, false));
            varTempNumber++;

        } else if (sem.equals("logicalAnd")) {
            // TODO: Implement logical and
        } else if (sem.equals("logicalOr")) {
            // TODO: Implement logical or
        } else if (sem.equals("createJump")) {
            String id = semanticStack.pop();
            labelTempNumber++;
            codeArray.add("\tbr i1 %" + id + ", label %" + labelTempNumber + ", label %");
            semanticStack.push(String.valueOf(codeSize));
        } else if (sem.equals("compJp")) {
            labelTempNumber++;
            codeArray.add("\tbr label %" + labelTempNumber);
            int jumpIndex = Integer.parseInt(semanticStack.pop());
            String code = codeArray.get(jumpIndex).concat(String.valueOf(labelTempNumber));
            codeArray.set(jumpIndex, code);
        } else if (sem.equals("elseJp")) {
            int jumpIndex = codeSize - 1;
            codeArray.set(jumpIndex, "\tbr label %");
            semanticStack.push(String.valueOf(jumpIndex));
        } else if (sem.equals("compElseJp")) {
            labelTempNumber++;
            int jumpIndex = Integer.parseInt(semanticStack.pop());
            codeArray.set(jumpIndex, codeArray.get(jumpIndex).concat(String.valueOf(labelTempNumber)));
        } else if (sem.equals("pushWhileLabel")) {
            labelTempNumber++;
            codeArray.add("\tbr label %" + labelTempNumber);
            semanticStack.push(String.valueOf(labelTempNumber));
        } else if (sem.equals("createWhileJump")) {
            String id = semanticStack.pop();
            int jumpLabel = Integer.parseInt(semanticStack.pop());
            labelTempNumber++;
            codeArray.add("\tbr i1 %" + id + ", label %" + jumpLabel + ", label %" + labelTempNumber);
        } else if (sem.equals("incrementCnt")) {
            tempCounter++;
        } else if (sem.equals("pushFunctionId")) {
            String id = (String) symbol.value;
            semanticStack.push(id);
            functionTable.put(id, new FDescription());
        } else if (sem.equals("addNumberOfArguments")) {
            functionTable.get(semanticStack.peek()).setNumberOfArguments(tempCounter);
        } else if (sem.equals("pushArgumentFunctionId")) {
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
        } else if (sem.equals("endFunction") | sem.equals("endProc")) {
            if (sem.equals("endProc"))
                codeArray.add("\tret void");
            codeArray.add("}");
            varTempNumber = 1;
        } else if (sem.equals("callWrite")) {
            String id = semanticStack.pop();
            String variableType = symbolTable.get(id).getType();
            String printParameter = getPrintParameter(variableType);
            codeArray.add("\t%_" + varTempNumber + " = call i32 (i8*, ...) @printf(" + printParameter + variableType + " %" + id + ")");
            symbolTable.put("_" + varTempNumber, new IDescription("i32", false));
            varTempNumber++;

        } else if (sem.equals("callRead")) {
            String id = (String) symbol.value;
            String variableType = symbolTable.get(id).getType();
            String printParameter = getPrintParameter(variableType);
            symbolTable.put("_" + varTempNumber, new IDescription("i32", false));
            codeArray.add("\t%_" + varTempNumber + " = call i32 (i8*, ...) @scanf(" + printParameter + variableType + "* %" + id + ")");
            varTempNumber++;

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
                    String stringValue = ((String) symbol.value).substring(1, ((String) symbol.value).length() - 1);
                    int fromIndex = 0;
                    int stringSize = stringValue.length() + 1;
                    while ((fromIndex = stringValue.indexOf("\\n", fromIndex)) != -1) {
                        fromIndex++;
                        stringSize--;
                    }
                    stringValue = stringValue.replace("\\n", "\\0A");
                    globalStringDefinition.add("@.str." + globalStrCounter + " = private unnamed_addr constant [" + stringSize + " x i8] c\""
                            + stringValue + "\\00\", align 1");
                    codeArray.add("\t%_" + varTempNumber + " = alloca i8*");

                    codeArray.add("\tstore i8* getelementptr inbounds ([" + stringSize + " x i8], [" + stringSize +
                            " x i8]* @.str." + globalStrCounter + ", i64 0, i64 0), i8** %_" + varTempNumber);
                    varTempNumber++;

                    codeArray.add("\t%_" + varTempNumber + " = load i8*, i8** %_" + (varTempNumber - 1));
                    symbolTable.put("_" + varTempNumber, new IDescription("i8*", false));
                    semanticStack.add("_" + varTempNumber);
                    varTempNumber++;
                    globalStrCounter++;

                    return;
            }
            codeArray.add("\t%_" + varTempNumber + " = alloca " + variableType);

            codeArray.add("\tstore " + variableType + " " + symbol.value + ", " + variableType + "* %_" + varTempNumber);

            varTempNumber++;
            codeArray.add("\t%_" + varTempNumber + " = load " + variableType + ", " + variableType + "* %_" + (varTempNumber - 1));
            symbolTable.put("_" + varTempNumber, new IDescription(variableType, false));
            semanticStack.add("_" + varTempNumber);
            varTempNumber++;

        } else if (sem.equals("reutrnId")) {
            String id = (String) symbol.value;
            String variableType = symbolTable.get(id).getType();
            codeArray.add("\t%_" + varTempNumber + " = load " + variableType + ", " + variableType + "* %" + id);
            symbolTable.put("_" + varTempNumber, new IDescription(variableType, false));

            codeArray.add("\tret " + variableType + " %_" + varTempNumber);
            varTempNumber++;

        } else if (sem.equals("returnConstant")) {
            String id = semanticStack.pop();
            String variableType = symbolTable.get(id).getType();
            codeArray.add("\tret " + variableType + " %" + id);
            varTempNumber++;

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
                    codeArray.add("\t%_" + varTempNumber + " = fsub 0, %" + id);
                } else {
                    codeArray.add("\t%_" + varTempNumber + " = sub 0, %" + id);
                }

            } else {
                if (!unaryOperationCheckType(variableType, id, "not"))
                    throw new Exception("Wrong variable type in not");
                codeArray.add("\t%_" + varTempNumber + " = xor -1, %" + id);

            }
            semanticStack.push("_" + varTempNumber);
            symbolTable.put("_" + varTempNumber, new IDescription(variableType, false));
            varTempNumber++;
        } else if (sem.equals("pushIdValue")) {
            tempId.push((String) symbol.value);
            String id = (String) symbol.value;
            String variableType = "";
            variableType = symbolTable.get(id).getType();
            codeArray.add("\t%_" + varTempNumber + " = load " + variableType + ", " + variableType + "* %" + id);

            semanticStack.push("_" + varTempNumber);
            symbolTable.put("_" + varTempNumber, new IDescription(variableType, false));
            varTempNumber++;
        } else if (sem.equals("pushArrayId")) {
            semanticStack.pop();
            codeArray.remove(codeArray.size() - 1);
            semanticStack.push(tempId.pop());
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
            codeArray.add("\t%_" + varTempNumber + " = " + loadPointer);

            semanticStack.push("_" + varTempNumber);
            symbolTable.put("_" + varTempNumber, new IDescription("i32", false));
            varTempNumber++;
        } else if (sem.equals("loadValue")) {
            String id = semanticStack.pop();
            String variableType = symbolTable.get(id).getType();
            codeArray.add("\t%_" + varTempNumber + " = load " + variableType + ", " + variableType + "* %" + id);

            symbolTable.put("_" + varTempNumber, new IDescription(variableType, false));
            semanticStack.add("_" + varTempNumber);
            varTempNumber++;
        } else if (sem.equals("callFunction")) {
            Stack<String> parameters = new Stack<String>();
            while (!functionTable.containsKey(semanticStack.peek())) {
                parameters.add(semanticStack.pop());
            }
            tempCounter = parameters.size();
            String functionId = semanticStack.pop();
            String functionReturnType = functionTable.get(functionId).getReturnType();
            StringBuilder code = new StringBuilder("\t");

            if (!functionReturnType.equals("void")) {
                code.append("%_").append(varTempNumber).append(" = ");
                symbolTable.put("_" + varTempNumber, new IDescription(functionReturnType, false));
                semanticStack.add("_" + varTempNumber);
                varTempNumber++;
            }
            code.append("call ").append(functionReturnType).append(" @").append(functionId).append("(");
            for (int i = 0; i < tempCounter; i++) {
                String id = parameters.pop();
                String variableType = symbolTable.get(id).getType();
                code.append(variableType).append(" %").append(id);
                if (i != tempCounter - 1)
                    code.append(", ");
            }
            code.append(")");
            codeArray.add(code.toString());
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
            printParameter = "i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.str.3, i32 0, i32 0), ";
        } else if (variableType.equals("i8*")) {
            printParameter = "i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.str.4, i64 0, i64 0), ";
        }
        return printParameter;
    }

    private void generateFunctionCode(String functionId) {
        String functionReturnType = functionTable.get(functionId).getReturnType();
        StringBuilder code = new StringBuilder("define " + functionReturnType + " @" + functionId + "(");
        List<String> variableTypes = functionTable.get(functionId).getParameterVariableTypes();
        List<String> variableIds = functionTable.get(functionId).getParameterIds();
        for (int i = 0; i < functionTable.get(functionId).getNumberOfArguments(); i++) {
            code.append(variableTypes.get(i)).append(" %f").append(variableIds.get(i));
            if (i < functionTable.get(functionId).getNumberOfArguments() - 1)
                code.append(", ");
        }
        code.append(") {");
        codeArray.add(code.toString());
        codeArray.add(";entry" + functionId + ":");
        symbolTable.put(functionId, new IDescription("i32", false));
        for (int i = 0; i < functionTable.get(functionId).getNumberOfArguments(); i++) {
            codeArray.add("\t%" + variableIds.get(i) + " = alloca " + variableTypes.get(i));
            codeArray.add("\tstore " + variableTypes.get(i) + " %f" + variableIds.get(i) + ", " + variableTypes.get(i) + "* %" + variableIds.get(i));
            symbolTable.put(variableIds.get(i), new IDescription(variableTypes.get(i), false));
        }
        labelTempNumber = 0;
    }

    public void printAllCode() {
        for (String code : codeArray) {
            System.out.println(code);
        }
    }

    public void addGlobalString() {
        codeArray.addAll(globalStringDefinition);
    }
}
