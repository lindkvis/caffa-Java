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
    }

    public static CaffaAbstractField create(CaffaObject owner, String dataType) {
        switch (dataType) {
            case "int": {
                return new CaffaField<Integer>(owner, Integer.class);
            }
            case "string": {
                return new CaffaField<String>(owner, String.class);
            }
            case "double": {
                return new CaffaField<Double>(owner, Double.class);
            }
            case "float": {
                return new CaffaField<Float>(owner, Float.class);
            }
            case "bool": {
                return new CaffaField<Boolean>(owner, Boolean.class);
            }
            case "int[]": {
                return new CaffaIntArrayField(owner);
            }
            case "string[]": {
                return new CaffaStringArrayField(owner);
            }
            case "double[]": {
                return new CaffaDoubleArrayField(owner);
            }
            case "float[]": {
                return new CaffaFloatArrayField(owner);
            }
            case "uint64[]": {
                return new CaffaLongArrayField(owner);
            }

        }
        System.err.println("Could not create array field for data type: " + dataType);
        return null;

    }

}
