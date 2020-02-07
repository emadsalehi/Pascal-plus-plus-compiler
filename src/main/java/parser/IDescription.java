package parser;

public class IDescription {

    private String type;
    private boolean isArray;

    public IDescription(String type, boolean isArray) {
        this.type = type;
        this.isArray = isArray;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isArray() {
        return isArray;
    }

    public void setArray(boolean array) {
        isArray = array;
    }
}
