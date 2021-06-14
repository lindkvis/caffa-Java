package org.caffa.rpc;

import org.caffa.rpc.CaffaField;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.lang.reflect.Type;
import java.lang.String;

public class CaffaFieldFactory {
    public static BiMap<String, Class<?>> dataTypes;

    static {
        dataTypes = HashBiMap.create();
        dataTypes.put("int", Integer.class);
        dataTypes.put("bool", Boolean.class);
        dataTypes.put("double", Double.class);
        dataTypes.put("float", Float.class);
        dataTypes.put("int64", Long.class);

    }
}
