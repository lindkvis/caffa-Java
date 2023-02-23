import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.IllegalArgumentException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.caffa.rpc.CaffaAppEnum;
import org.caffa.rpc.CaffaAppEnumField;
import org.caffa.rpc.CaffaField;
import org.caffa.rpc.CaffaObject;
import org.caffa.rpc.GrpcClientApp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClientFieldTest {
    private GrpcClientApp testApp;

    @BeforeEach
    public void setUp() throws Exception {
        testApp = new GrpcClientApp("localhost", 50000, "log4j.properties");
    }

    @AfterEach
    public void cleanUp() {
        testApp.cleanUp();
    }

    @Test
    void getDocumentFileName() {
        CaffaObject object = assertDoesNotThrow(() -> testApp.document("testDocument"));
        assertTrue(object != null);
        assertTrue(!object.fields().isEmpty());

        System.out.println(object.dump());

        String key = "fileName";
        CaffaField<?> field = object.field(key);
        assertNotNull(field);
        assertEquals(key, field.keyword);

        CaffaField<String> fileNameField = field.cast(String.class);
        String originalValue = fileNameField.get();
        assertEquals("dummyFileName", originalValue);
        assertDoesNotThrow(() -> fileNameField.set("TestValue"));
        String value = fileNameField.get();
        assertEquals("TestValue", value);
        assertDoesNotThrow(() -> fileNameField.set(originalValue));
        assertEquals(originalValue, fileNameField.get());
    }

    @Test
    void documentFields() {
        CaffaObject object = assertDoesNotThrow(() -> testApp.document("testDocument"));
        assertTrue(!object.fields().isEmpty());

        Boolean foundDocumentFileName = false;
        for (CaffaField<?> field : object.fields()) {
            System.out.println("Found field: '" + field.keyword + "' (" + field.type() +
                    ")");
            if (field.keyword.equals("fileName")) {
                foundDocumentFileName = true;
                assertEquals(String.class, field.type());
            }
        }
        assertTrue(foundDocumentFileName);
    }

    @Test
    void floatVector() {
        CaffaObject object = assertDoesNotThrow(() -> testApp.document("testDocument"));

        CaffaObject demoObject = object.field("demoObject", CaffaObject.class).get();
        assertNotNull(demoObject);
        System.out.println("Check which field was actually created:");
        CaffaField<?> floatArrayField = demoObject.field("floatVector");
        assertNotNull(floatArrayField);
        System.out.println(floatArrayField.dump());
        CaffaField<Float[]> typedFloatArrayField = floatArrayField.cast(Float[].class);
        assertNotNull(typedFloatArrayField);

        {
            Float[] values2 = { 41.4f, 42.0f, -23.0f, -82.0f };
            assertDoesNotThrow(() -> typedFloatArrayField.set(values2));
        }
        Float[] values = typedFloatArrayField.get();
        assertTrue(values.length == 4);
        assertTrue(values[3] == -82.0f);
    }

    @Test
    void intVector() {
        CaffaObject object = assertDoesNotThrow(() -> testApp.document("testDocument"));
        CaffaObject demoObjectOriginal = object.field("demoObject", CaffaObject.class).get();
        CaffaObject demoObject = object.field("demoObject", CaffaObject.class).get();
        assertNotNull(demoObject);
        System.out.println("Check which field was actually created:");
        CaffaField<?> intArrayFieldOriginal = demoObjectOriginal.field("proxyIntVector");
        CaffaField<?> intArrayField = demoObject.field("proxyIntVector");
        assertNotNull(intArrayFieldOriginal);
        assertNotNull(intArrayField);
        System.out.println(intArrayField.dump());
        CaffaField<Integer[]> typedIntArrayField = intArrayField.cast(Integer[].class);
        assertNotNull(typedIntArrayField);

        Integer[] values = typedIntArrayField.get();

        {
            Integer[] values2 = { 44, 43, 172 };

            assertDoesNotThrow(() -> typedIntArrayField.set(values2));

            Integer[] values3 = typedIntArrayField.get();
            assertTrue(Arrays.equals(values3, values2));

            Integer[] values4 = intArrayFieldOriginal.get(Integer[].class);
            assertTrue(Arrays.equals(values3, values4));
        }
        assertDoesNotThrow(() -> typedIntArrayField.set(values));

        System.out.print("\n");
    }

    @Test
    void boolVector() {
        CaffaObject object = assertDoesNotThrow(() -> testApp.document("testDocument"));
        CaffaObject demoObject = object.field("demoObject", CaffaObject.class).get();
        assertNotNull(demoObject);
        System.out.println("Check which field was actually created:");
        CaffaField<?> boolArrayField = demoObject.field("boolVector");
        assertNotNull(boolArrayField);
        System.out.println(boolArrayField.dump());
        CaffaField<Boolean[]> typedBoolArrayField = boolArrayField.cast(Boolean[].class);
        assertNotNull(typedBoolArrayField);
        Boolean[] values = typedBoolArrayField.get();

        {
            Boolean[] values2 = { true, false, true };
            Boolean[] mergedValues = Arrays.copyOf(values, values.length + values2.length);
            System.arraycopy(values2, 0, mergedValues, values.length, values2.length);
            assertDoesNotThrow(() -> typedBoolArrayField.set(mergedValues));

            Boolean[] values3 = typedBoolArrayField.get();
            assertEquals(mergedValues.length, values3.length);
            assertTrue(Arrays.equals(values3, mergedValues));
        }

        assertDoesNotThrow(() -> typedBoolArrayField.set(values));
    }

    @Test
    void doubleMember() {
        CaffaObject object = assertDoesNotThrow(() -> testApp.document(""));

        CaffaObject demoObject = object.field("demoObject", CaffaObject.class).get();
        assertNotNull(demoObject);
        System.out.println("Check that a double field was actually created:");
        System.out.println(demoObject.field("doubleField").dump());
        CaffaField<?> untypedDoubleField = demoObject.field("doubleField");
        CaffaField<Double> doubleField = untypedDoubleField.cast(Double.class);
        Double originalValue = doubleField.get();
        System.out.println("Original double value: " + originalValue);
        Double newValue = 45.3;
        assertDoesNotThrow(() -> doubleField.set(newValue));
        System.out.println("Setting double value: " + newValue);

        assertEquals(newValue, doubleField.get());
        System.out.println("Confirmed values match!");

        assertDoesNotThrow(() -> doubleField.set(originalValue));
        assertEquals(originalValue, doubleField.get());

    }

    @Test
    void appEnumMember() {
        CaffaObject object = assertDoesNotThrow(() -> testApp.document(""));

        CaffaObject demoObject = object.field("demoObject", CaffaObject.class).get();
        assertNotNull(demoObject);
        System.out.println(demoObject.field("enumField").dump());
        CaffaField<?> untypedEnumField = demoObject.field("enumField");
        CaffaAppEnumField enumField = untypedEnumField.cast(CaffaAppEnumField.class,
                CaffaAppEnum.class);
        CaffaAppEnum originalValue = enumField.get();

        assertDoesNotThrow(() -> enumField.set("T3"));

        assertEquals("T3", enumField.get().value());

        assertThrows(IllegalArgumentException.class, () -> enumField.set("T4"));

        assertDoesNotThrow(() -> enumField.set(originalValue));
        assertEquals(originalValue.value(), enumField.get().value());
    }

    @Test
    void deepCopy() {
        CaffaObject object = assertDoesNotThrow(() -> testApp.document("testDocument"));
        CaffaObject demoObjectOriginal = object.field("demoObject", CaffaObject.class).get();
        CaffaObject demoObject = object.field("demoObject", CaffaObject.class).deepClone();

        assertNotNull(demoObject);
        System.out.println("Check which field was actually created:");
        CaffaField<?> intArrayFieldOriginal = demoObjectOriginal.field("proxyIntVector");
        CaffaField<?> intArrayField = demoObject.field("proxyIntVector");
        assertNotNull(intArrayField);
        System.out.println(intArrayField.dump());
        CaffaField<Integer[]> typedIntArrayField = intArrayField.cast(Integer[].class);
        assertNotNull(typedIntArrayField);

        Integer[] values = typedIntArrayField.get();
        {
            Integer[] values2 = { 44, 43, 172 };

            assertDoesNotThrow(() -> typedIntArrayField.set(values2));

            Integer[] values3 = typedIntArrayField.get();
            assertTrue(Arrays.equals(values3, values2));

            // Values should not have been sent to server
            Integer[] valuesFromOriginal = intArrayFieldOriginal.get(Integer[].class);
            assertTrue(!Arrays.equals(values3, valuesFromOriginal));

            // Now copy the values to the server
            assertDoesNotThrow(() -> object.field("demoObject", CaffaObject.class).deepCopyFrom(demoObject));
            valuesFromOriginal = intArrayFieldOriginal.get(Integer[].class);
            assertTrue(Arrays.equals(values3, valuesFromOriginal));
        }

        assertDoesNotThrow(() -> typedIntArrayField.set(values));

        System.out.print("\n");
    }

}
