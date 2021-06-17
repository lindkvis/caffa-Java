package org.caffa.rpc;

import org.caffa.rpc.CaffaArrayField;
import org.caffa.rpc.CaffaField;
import org.caffa.rpc.CaffaObject;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.lang.reflect.Type;
import java.lang.String;
import java.util.HashMap;
import java.util.Map;

public class CaffaFieldFactory {
    public static BiMap<String, Class<?>> dataTypes;
    public static Map<String, CaffaAbstractField> fieldCreators;
    public static Map<String, CaffaAbstractField> arrayFieldCreators;

    static {
        dataTypes = HashBiMap.create();
        fieldCreators = new HashMap<String, CaffaAbstractField>();
        arrayFieldCreators = new HashMap<String, CaffaAbstractField>();

        addCreator("int", Integer.class);
        addCreator("double", Double.class);
        addCreator("float", Float.class);
        addCreator("uint64", Long.class);
        addCreator("string", String.class);
        addCreator("object", CaffaObject.class);
    }

    public static <T> void addCreator(String typeName, Class<T> clazz) {
        dataTypes.put(typeName, clazz);
        fieldCreators.put(typeName, new CaffaField<T>(null, "", clazz));
        arrayFieldCreators.put(typeName, createArrayField(clazz));
    }

    public static CaffaAbstractField createArrayField(Class<?> clazz) {
        if (clazz == Integer.class) {
            return new CaffaIntArrayField(null, "");
        } else if (clazz == Double.class) {
            return new CaffaDoubleArrayField(null, "");
        } else if (clazz == Float.class) {
            return new CaffaFloatArrayField(null, "");
        } else if (clazz == Long.class) {
            return new CaffaLongArrayField(null, "");
        } else if (clazz == String.class) {
            return new CaffaStringArrayField(null, "");
        } else if (clazz == CaffaObject.class) {
            return new CaffaObjectArrayField(null, "");
        }
        System.err.println("Could not create array field!");
        return null;
    }

    public static CaffaAbstractField createField(CaffaObject owner, String keyword, String dataType) {
        return fieldCreators.get(dataType).newInstance(owner, keyword);
    }

    public static CaffaAbstractField createArrayField(CaffaObject owner, String keyword, String dataType) {
        CaffaAbstractField creator = arrayFieldCreators.get(dataType);
        return creator.newInstance(owner, keyword);
    }
}