package org.caffa.rpc;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class CaffaFieldFactory {
    public static BiMap<String, Class<?>> dataTypes;
    public static Map<String, CaffaField<?>> fieldCreators;
    protected static final Logger logger = LoggerFactory.getLogger(CaffaFieldFactory.class);

    static {
        dataTypes = HashBiMap.create();
        fieldCreators = new HashMap<String, CaffaField<?>>();

        addCreator("bool", Boolean.class);
        addCreator("int32", Integer.class);
        addCreator("double", Double.class);
        addCreator("float", Float.class);
        addCreator("int64", Long.class);
        addCreator("string", String.class);

        addCreator("bool[]", Boolean[].class);
        addCreator("int32[]", Integer[].class);
        addCreator("double[]", Double[].class);
        addCreator("float[]", Float[].class);
        addCreator("int64[]", Long[].class);
        addCreator("string[]", String[].class);

        addCreator("object", CaffaObject.class);
        addCreator("object[]", CaffaObject[].class);

        dataTypes.put("AppEnum", CaffaAppEnum.class);
        fieldCreators.put("AppEnum", new CaffaAppEnumField(null, ""));
    }

    public static <T> void addCreator(String typeName, Class<T> clazz) {
        dataTypes.put(typeName, clazz);
        fieldCreators.put(typeName, new CaffaField<>(null, "", clazz));
    }

    public static CaffaField<?> createField(CaffaObject owner, String keyword, String dataType) {
        logger.debug("Creating field of type " + dataType);
        return createFieldWithCreators(owner, keyword, dataType, fieldCreators);
    }

    private static CaffaField<?> createFieldWithCreators(CaffaObject owner, String keyword, String dataType,
            Map<String, CaffaField<?>> creators) {

        if (dataType.startsWith("uint")) {
            CaffaField<?> creator = creators.get(dataType.replace("uint", "int"));
            if (creator == null) {
                logger.error("Could not find creator for dataType: " + dataType);
                return null;
            }
            CaffaField<?> unsignedField = creator.newInstance(owner,
                    keyword);
            unsignedField.setUnsigned(true);
            assert unsignedField.getUnsigned();

            return unsignedField;
        }
        if (dataType.startsWith("AppEnum")) {
            CaffaField<?> creator = creators.get("AppEnum");
            if (creator == null) {
                logger.error("Could not find creator for dataType: " + dataType);
                return null;
            }
            return (CaffaAppEnumField) creator.newInstance(owner, keyword);
        }

        CaffaField<?> creator = creators.get(dataType);
        if (creator == null) {
            logger.error("Could not find creator for dataType: " + dataType);
            return null;
        }
        return creator.newInstance(owner, keyword);
    }
}
