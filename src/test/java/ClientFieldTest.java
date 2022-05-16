import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.IllegalArgumentException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.caffa.rpc.CaffaAppEnum;
import org.caffa.rpc.CaffaAppEnumField;
import org.caffa.rpc.CaffaBooleanArrayField;
import org.caffa.rpc.CaffaField;
import org.caffa.rpc.CaffaFloatArrayField;
import org.caffa.rpc.CaffaIntArrayField;
import org.caffa.rpc.CaffaObject;
import org.caffa.rpc.GrpcClientApp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClientFieldTest {
    private GrpcClientApp testApp;

    @BeforeAll
    public static void logSetup() {
        Logger.getGlobal().setLevel(Level.INFO);
    }

    @BeforeEach
    public void setUp() throws Exception {
        testApp = new GrpcClientApp("localhost", 50000);
    }

    @AfterEach
    public void cleanUp() {
        testApp.cleanUp();
    }

    @Test
    void getDocumentFileName() {
        CaffaObject object = testApp.document("testDocument");

        assertTrue(!object.fields.isEmpty());

        object.dump();

        String key = "fileName";
        assertTrue(object.fields.containsKey(key));
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
        CaffaObject object = testApp.document("testDocument");
        assertTrue(!object.fields.isEmpty());

        Boolean foundDocumentFileName = false;
        for (Map.Entry<String, CaffaField<?>> entry : object.fields.entrySet()) {
            CaffaField<?> field = entry.getValue();
            assertEquals(field.keyword, entry.getKey());
            System.out.println("Found field: '" + entry.getKey() + "' (" + field.type() + ")");
            if (field.keyword.equals("fileName")) {
                foundDocumentFileName = true;
                assertEquals(String.class, field.type());
            }
        }
        assertTrue(foundDocumentFileName);
    }

    @Test
    void floatVector() {
        CaffaObject object = testApp.document("testDocument");
        System.out.println("Getting children!");
        List<CaffaObject> children = object.children();
        System.out.println("Got children!");
        assertTrue(!children.isEmpty());
        CaffaObject demoObject = children.get(0);
        System.out.println("Check which field was actually created:");
        CaffaField<?> floatArrayField = demoObject.field("floatVector");
        assertNotNull(floatArrayField);
        floatArrayField.dump();
        CaffaFloatArrayField typedFloatArrayField = floatArrayField.cast(CaffaFloatArrayField.class, Float.class);
        assertNotNull(typedFloatArrayField);

        {
            ArrayList<Float> values2 = new ArrayList<Float>();
            values2.add(41.4f);
            values2.add(42.0f);
            values2.add(-23.0f);
            typedFloatArrayField.set(values2);
        }
        List<Float> values = typedFloatArrayField.get();
        assertTrue(!values.isEmpty());
    }

    @Test
    void intVector() {
        CaffaObject object = testApp.document("testDocument");
        System.out.println("Getting children!");
        List<CaffaObject> children = object.children();
        System.out.println("Got children!");
        assertTrue(!children.isEmpty());
        CaffaObject demoObject = children.get(0);
        System.out.println("Check which field was actually created:");
        CaffaField<?> intArrayField = demoObject.field("proxyIntVector");
        assertNotNull(intArrayField);
        intArrayField.dump();
        CaffaIntArrayField typedIntArrayField = intArrayField.cast(CaffaIntArrayField.class, Integer.class);
        assertNotNull(typedIntArrayField);

        ArrayList<Integer> values = typedIntArrayField.get();

        {
            ArrayList<Integer> values2 = new ArrayList<Integer>();
            values2.add(44);
            values2.add(43);
            values2.add(172);

            typedIntArrayField.set(values2);

            ArrayList<Integer> values3 = typedIntArrayField.get();
            assertTrue(!values3.isEmpty());
            assertEquals(values2, values3);
        }
        typedIntArrayField.set(values);

        System.out.print("\n");
    }

    @Test
    void boolVector() {
        CaffaObject object = testApp.document("testDocument");
        System.out.println("Getting children!");
        List<CaffaObject> children = object.children();
        System.out.println("Got children!");
        assertTrue(!children.isEmpty());
        CaffaObject demoObject = children.get(0);
        System.out.println("Check which field was actually created:");
        CaffaField<?> boolArrayField = demoObject.field("boolVector");
        assertNotNull(boolArrayField);
        boolArrayField.dump();
        CaffaBooleanArrayField typedBoolArrayField = boolArrayField.cast(CaffaBooleanArrayField.class, Boolean.class);
        assertNotNull(typedBoolArrayField);
        ArrayList<Boolean> values = typedBoolArrayField.get();

        {
            ArrayList<Boolean> values2 = new ArrayList<Boolean>();
            values2.addAll(values);
            values2.add(true);
            values2.add(false);
            values2.add(true);

            typedBoolArrayField.set(values2);

            ArrayList<Boolean> values3 = typedBoolArrayField.get();
            assertEquals(values2.size(), values3.size());
            assertEquals(values2, values3);
        }
        typedBoolArrayField.set(values);
    }

    @Test
    void doubleMember() {
        CaffaObject object = testApp.document("");

        List<CaffaObject> children = object.children();
        assertTrue(!children.isEmpty());
        CaffaObject demoObject = children.get(0);
        System.out.println("Check that a double field was actually created:");
        demoObject.field("doubleField").dump();
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
        CaffaObject object = testApp.document("");

        List<CaffaObject> children = object.children();
        assertTrue(!children.isEmpty());
        CaffaObject demoObject = children.get(0);
        demoObject.field("enumField").dump();
        CaffaField<?> untypedEnumField = demoObject.field("enumField");
        CaffaAppEnumField enumField = untypedEnumField.cast(CaffaAppEnumField.class, CaffaAppEnum.class);
        CaffaAppEnum originalValue = enumField.get();

        assertDoesNotThrow(() -> enumField.set("T3"));

        assertEquals("T3", enumField.get().value());

        assertThrows(IllegalArgumentException.class, () -> enumField.set("T4"));

        assertDoesNotThrow(() -> enumField.set(originalValue));
        assertEquals(originalValue.value(), enumField.get().value());
    }
}
