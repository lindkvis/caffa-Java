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

        addCreator("boolean", Boolean.class);
        addCreator("integer", Long.class);
        addCreator("number", Double.class);
        addCreator("string", String.class);

        addCreator("boolean[]", Boolean[].class);
        addCreator("integer[]", Long[].class);
        addCreator("number[]", Double[].class);
        addCreator("string[]", String[].class);

        addCreator("object", CaffaObject.class);
        addCreator("object[]", CaffaObject[].class);

        dataTypes.put("enum", CaffaAppEnum.class);
        fieldCreators.put("enum", new CaffaAppEnumField(null, ""));
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
        if (dataType.startsWith("enum")) {
            CaffaField<?> creator = creators.get("enum");
            if (creator == null) {
                logger.error("Could not find creator for dataType: " + dataType);
                return null;
            }
            CaffaAppEnumField appEnumField = (CaffaAppEnumField) creator.newInstance(owner, keyword);

            String validValuesString = dataType.substring(5, dataType.length() - 1);
            String[] values = validValuesString.split(",");
            for (String value : values) {
                String trimmedValue = value.replaceAll("^\"|\"$", "");
                appEnumField.addValidValue(trimmedValue);
            }

            return appEnumField;
        }

        CaffaField<?> creator = creators.get(dataType);
        if (creator == null) {
            logger.error("Could not find creator for dataType: " + dataType);
            return null;
        }
        return creator.newInstance(owner, keyword);
    }
}
