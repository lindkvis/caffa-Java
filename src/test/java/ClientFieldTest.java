import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.caffa.rpc.CaffaBooleanArrayField;
import org.caffa.rpc.CaffaField;
import org.caffa.rpc.CaffaFloatArrayField;
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
        testApp = new GrpcClientApp("localhost", 55555);
    }

    @AfterEach
    public void cleanUp() {
        testApp.cleanUp();
    }

    @Test
    void getDocumentFileName() {
        CaffaObject object = testApp.document("DemoDocument");

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
        fileNameField.set("TestValue");
        String value = fileNameField.get();
        assertEquals("TestValue", value);
        fileNameField.set(originalValue);
        assertEquals(originalValue, fileNameField.get());
    }

    @Test
    void documentFields() {
        CaffaObject object = testApp.document("DemoDocument");
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
        CaffaObject object = testApp.document("DemoDocument");
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
        ArrayList<Float> values = typedFloatArrayField.get();
        assertTrue(!values.isEmpty());

        System.out.print("Printing first ten floats: ");
        for (int i = 0; i < 10; ++i) {
            System.out.print(values.get(i) + " ");
        }
        System.out.print("\n");

        {
            int count = 10000;
            ArrayList<Float> newValues = new ArrayList<Float>();
            for (int i = 0; i < count; ++i) {
                newValues.add((float) i);
            }
            typedFloatArrayField.set(newValues);

            ArrayList<Float> readValues = typedFloatArrayField.get();
            assertEquals(count, readValues.size());
            for (int i = 0; i < count; ++i) {
                assertEquals((float) i, readValues.get(i));
            }
        }

        {
            int count = 20;
            ArrayList<Float> newValues = new ArrayList<Float>();
            for (int i = 0; i < count; ++i) {
                newValues.add((float) i);
            }
            typedFloatArrayField.set(newValues);

            ArrayList<Float> readValues = typedFloatArrayField.get();
            assertEquals(count, readValues.size());
            for (int i = 0; i < count; ++i) {
                System.out.print(readValues.get(i) + " ");
                assertEquals((float) i, readValues.get(i));
            }
        }
        // Original values
        typedFloatArrayField.set(values);

    }

    @Test
    void boolVector() {
        CaffaObject object = testApp.document("DemoDocument");
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

        assertEquals(4, values.size());
        assertEquals(true, values.get(0));
        assertEquals(false, values.get(1));
        assertEquals(false, values.get(2));
        assertEquals(true, values.get(3));

        {
            ArrayList<Boolean> newValues = new ArrayList<Boolean>() {
                {
                    add(false);
                    add(false);
                    add(true);
                }
            };
            typedBoolArrayField.set(newValues);
        }
        {
            ArrayList<Boolean> newValues = typedBoolArrayField.get();
            assertEquals(3, newValues.size());
            assertEquals(false, newValues.get(0));
            assertEquals(false, newValues.get(1));
            assertEquals(true, newValues.get(2));
        }
        // Original values
        typedBoolArrayField.set(values);
    }

    @Test
    void doubleMember() {
        CaffaObject object = testApp.document("");

        List<CaffaObject> children = object.children();
        assertTrue(!children.isEmpty());
        CaffaObject demoObject = children.get(0);
        System.out.println("Check that a double field was actually created:");
        demoObject.field("doubleMember").dump();
        CaffaField<?> untypedDoubleField = demoObject.field("doubleMember");
        CaffaField<Double> doubleField = untypedDoubleField.cast(Double.class);
        Double originalValue = doubleField.get();
        System.out.println("Original double value: " + originalValue);
        Double newValue = 45.3;
        doubleField.set(newValue);
        System.out.println("Setting double value: " + newValue);

        assertEquals(newValue, doubleField.get());
        System.out.println("Confirmed values match!");

        doubleField.set(originalValue);
        assertEquals(originalValue, doubleField.get());
    }
}
