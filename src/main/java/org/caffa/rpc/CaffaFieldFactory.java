package org.caffa.rpc;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class CaffaFieldFactory {
    public static BiMap<String, Class<?>> dataTypes;
    public static Map<String, CaffaField<?>> fieldCreators;
    public static Map<String, CaffaField<?>> arrayFieldCreators;
    protected static final Logger logger = Logger.getLogger(CaffaFieldFactory.class.getName());

    static {
        dataTypes = HashBiMap.create();
        fieldCreators = new HashMap<String, CaffaField<?>>();
        arrayFieldCreators = new HashMap<String, CaffaField<?>>();

        addCreator("bool", Boolean.class);
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

    public static CaffaField<?> createArrayField(Class<?> clazz) {
        if (clazz == Boolean.class) {
            return new CaffaBooleanArrayField(null, "");
        }
        else if (clazz == Integer.class) {
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
        logger.log(Level.SEVERE, "Could not create array field!");
        return null;
    }

    public static CaffaField<?> createField(CaffaObject owner, String keyword, String dataType) {
        logger.log(Level.FINER, "Creating field of type " + dataType);
        return fieldCreators.get(dataType).newInstance(owner, keyword);
    }

    public static CaffaField<?> createArrayField(CaffaObject owner, String keyword, String dataType) {
        CaffaField<?> creator = arrayFieldCreators.get(dataType);
        return creator.newInstance(owner, keyword);
    }
}