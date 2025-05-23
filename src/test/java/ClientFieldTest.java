import java.util.Arrays;

import org.caffa.rpc.CaffaAppEnum;
import org.caffa.rpc.CaffaAppEnumField;
import org.caffa.rpc.CaffaField;
import org.caffa.rpc.CaffaObject;
import org.caffa.rpc.RestClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClientFieldTest {
    private RestClient testApp;
    private final String hostname = "127.0.0.1";

    @BeforeEach
    public void setUp() throws Exception {
        testApp = new RestClient(hostname, 50000, "log4j.properties");
        testApp.connect("test", "password");
    }

    @AfterEach
    public void cleanUp() {
        testApp.cleanUp();
    }

    @Test
    void getDocumentId() {
        CaffaObject object = assertDoesNotThrow(() -> testApp.document("testDocument"));
        assertNotNull(object);
        assertFalse(object.fields().isEmpty());

        System.out.println(object.dump());

        String key = "id";
        CaffaField<?> field = object.field(key);
        assertNotNull(field);
        assertEquals(key, field.keyword());

        CaffaField<String> idField = field.cast(String.class);
        String originalValue = idField.get();
        assertEquals("testDocument", originalValue);
        assertThrows(Exception.class, () -> idField.set("TestValue"));
        String value = idField.get();
        assertEquals("testDocument", value);        
    }

    @Test
    void documentFields() {
        CaffaObject object = assertDoesNotThrow(() -> testApp.document("testDocument"));
        assertFalse(object.fields().isEmpty());

        boolean foundIdField = false;
        for (CaffaField<?> field : object.fields()) {
            System.out.println("Found field: '" + field.keyword() + "' (" + field.type() +
                    ")");
            if (field.keyword().equals("id")) {
                foundIdField = true;
                assertEquals(String.class, field.type());
            }
        }
        assertTrue(foundIdField);
    }

    @Test
    void doubleVector() {
        CaffaObject object = assertDoesNotThrow(() -> testApp.document("testDocument"));

        CaffaObject demoObject = object.field("demoObject", CaffaObject.class).get();
        assertNotNull(demoObject);
        System.out.println("Check which field was actually created:");
        CaffaField<?> floatArrayField = demoObject.field("floatVector");
        assertNotNull(floatArrayField);
        System.out.println(floatArrayField.dump());
        CaffaField<Double[]> typedFloatArrayField = floatArrayField.cast(Double[].class);
        assertNotNull(typedFloatArrayField);

        {
            Double[] values2 = { 41.4, 42.0, -23.0, -82.0 };
            assertDoesNotThrow(() -> typedFloatArrayField.set(values2));
        }
        Double[] values = typedFloatArrayField.get();
        assertNotNull(values);
        assertEquals(4, values.length);
        assertEquals(-82.0f, values[3]);
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
        CaffaField<Long[]> typedIntArrayField = intArrayField.cast(Long[].class);
        assertNotNull(typedIntArrayField);

        Long[] values = typedIntArrayField.get();

        {
            Long[] values2 = { 44L, 43L, 172L };

            assertDoesNotThrow(() -> typedIntArrayField.set(values2));

            Long[] values3 = typedIntArrayField.get();
            assertArrayEquals(values3, values2);

            Long[] values4 = intArrayFieldOriginal.get(Long[].class);
            assertArrayEquals(values3, values4);
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
        assertNotNull(values);

        {
            Boolean[] values2 = { true, false, true };
            Boolean[] mergedValues = Arrays.copyOf(values, values.length + values2.length);
            System.arraycopy(values2, 0, mergedValues, values.length, values2.length);
            assertDoesNotThrow(() -> typedBoolArrayField.set(mergedValues));

            Boolean[] values3 = typedBoolArrayField.get();
            assertEquals(mergedValues.length, values3.length);
            assertArrayEquals(values3, mergedValues);
        }

        assertDoesNotThrow(() -> typedBoolArrayField.set(values));
    }

    @Test
    void doubleMember() {
        CaffaObject object = assertDoesNotThrow(() -> testApp.document("testDocument"));

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
        CaffaObject object = assertDoesNotThrow(() -> testApp.document("testDocument"));

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
        CaffaField<Long[]> typedIntArrayField = intArrayField.cast(Long[].class);
        assertNotNull(typedIntArrayField);

        {
            Long[] values2 = { 54L, 83L, 272L, 12L };

            assertDoesNotThrow(() -> typedIntArrayField.set(values2));

            Long[] values3 = typedIntArrayField.get();
            assertArrayEquals(values3, values2);

            // Values should not have been sent to server
            Long[] valuesFromOriginal = intArrayFieldOriginal.get(Long[].class);
            assertFalse(Arrays.equals(values3, valuesFromOriginal));
        }

        
        System.out.print("\n");
    }

}
