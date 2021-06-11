package org.caffa.rpc;

import org.caffa.rpc.CaffaAbstractField;

import java.util.ArrayList;

public class CaffaArrayField<DataType> extends CaffaAbstractField {

    public void dump() {
        System.out.println("keyword = " + keyword);
        System.out.println("type = " + type + " [array]");

    }
}
