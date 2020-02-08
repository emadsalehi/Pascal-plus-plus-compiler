package code_generator;

import java.util.ArrayList;
import java.util.List;

public class FDescription {

    int numberOfArguments = 0;
    String returnType;
    List<String> parameterVariableTypes = new ArrayList<String>();
    List<String> parameterIds = new ArrayList<String>();


    public List<String> getParameterIds() {
        return parameterIds;
    }

    public void setParameterIds(List<String> parameterIds) {
        this.parameterIds = parameterIds;
    }


    public FDescription() {
    }

    public FDescription(String returnType) {
        this.returnType = returnType;
    }

    public int getNumberOfArguments() {
        return numberOfArguments;
    }

    public void setNumberOfArguments(int numberOfArguments) {
        this.numberOfArguments = numberOfArguments;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public List<String> getParameterVariableTypes() {
        return parameterVariableTypes;
    }

    public void setParameterVariableTypes(List<String> parameterVariableTypes) {
        this.parameterVariableTypes = parameterVariableTypes;
    }

    public void addParameterVariableType(String variableType) {
        parameterVariableTypes.add(variableType);
    }

    public void addParameterName(String variableName) {
        parameterIds.add(variableName);
    }
}
