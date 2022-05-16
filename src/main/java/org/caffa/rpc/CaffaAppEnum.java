package org.caffa.rpc;

public class CaffaAppEnum {
    private String value;

    public CaffaAppEnum() {
        this.value = "";
    }

    public CaffaAppEnum(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
