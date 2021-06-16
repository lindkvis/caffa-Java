package org.caffa.rpc;

import org.caffa.rpc.CaffaField;
import org.caffa.rpc.Object;

import io.grpc.ManagedChannel;

import com.google.gson.annotations.Expose;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.TreeMap;
import java.util.Map;

public class CaffaObject {
    @Expose
    public String classKeyword;
    @Expose
    public long serverAddress;

    public Map<String, CaffaAbstractField> fields;

    public ManagedChannel channel;

    public CaffaObject(ManagedChannel channel) {
        this.channel = channel;
        this.fields = new TreeMap<String, CaffaAbstractField>();
    }

    public void dump() {
        System.out.println("CaffaObject {");
        System.out.println("fields = [");
        for (Map.Entry<String, CaffaAbstractField> entry : this.fields.entrySet()) {
            System.out.print(entry.getKey() + " = ");
            entry.getValue().dump();
        }
        System.out.println("]");
        System.out.println("}");
    }
}
