package org.caffa.rpc;

import org.caffa.rpc.CaffaField;
import org.caffa.rpc.CaffaObject;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.lang.reflect.Type;
import java.lang.String;

public class CaffaFieldFactory {
    public static BiMap<String, Class<?>> dataTypes;

    static {
        dataTypes = HashBiMap.create();
        dataTypes.put("int", Integer.class);
        dataTypes.put("double", Double.class);
        dataTypes.put("float", Float.class);
        dataTypes.put("uint64", Long.class);
        dataTypes.put("string", String.class);
        dataTypes.put("object", CaffaObject.class);
    }

    public static CaffaAbstractField create(CaffaObject owner, String keyword, String dataType) {
        switch (dataType) {
            case "int": {
                return new CaffaField<Integer>(owner, keyword, Integer.class);
            }
            case "string": {
                return new CaffaField<String>(owner, keyword, String.class);
            }
            case "double": {
                return new CaffaField<Double>(owner, keyword, Double.class);
            }
            case "float": {
                return new CaffaField<Float>(owner, keyword, Float.class);
            }
            case "uint64": {
                return new CaffaField<Long>(owner, keyword, Long.class);
            }
            case "int[]": {
                return new CaffaIntArrayField(owner, keyword);
            }
            case "string[]": {
                return new CaffaStringArrayField(owner, keyword);
            }
            case "double[]": {
                return new CaffaDoubleArrayField(owner, keyword);
            }
            case "float[]": {
                return new CaffaFloatArrayField(owner, keyword);
            }
            case "uint64[]": {
                return new CaffaLongArrayField(owner, keyword);
            }
        }
        System.err.println("Could not create array field for data type: " + dataType);
        return null;

    }

}
